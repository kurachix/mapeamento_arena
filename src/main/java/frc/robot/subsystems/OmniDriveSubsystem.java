/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import com.studica.frc.Titan;
import frc.robot.Constants;

public class OmniDriveSubsystem extends SubsystemBase {

    // Controladora Titan Quad
    private final Titan titan;

    // Motores: M0 = Elevador, M1 = Direita, M2 = Tras, M3 = Esquerda
    private final Titan.Motor motorRight;
    private final Titan.Motor motorRear;
    private final Titan.Motor motorElevator;
    private final Titan.Motor motorLeft;

    // Encoders correspondentes na Titan Quad
    private final Titan.Encoder encoderRight;
    private final Titan.Encoder encoderRear;
    private final Titan.Encoder encoderElevator;
    private final Titan.Encoder encoderLeft;

    // Leituras de pulsos cacheadas por ciclo
    private double currentTicksRight    = 0.0;
    private double currentTicksRear     = 0.0;
    private double currentTicksElevator = 0.0;
    private double currentTicksLeft     = 0.0;

    // Offsets de software para garantir zeramento confiavel e imediato
    private double offsetTicksRight    = 0.0;
    private double offsetTicksRear     = 0.0;
    private double offsetTicksElevator = 0.0;
    private double offsetTicksLeft     = 0.0;

    public OmniDriveSubsystem() {
        titan = new Titan(Constants.kTitanCanId);

        // Inicializacao dos Motores
        motorRight    = titan.getMotor(Constants.kMotorRightPort);
        motorRear     = titan.getMotor(Constants.kMotorRearPort);
        motorElevator = titan.getMotor(Constants.kMotorElevatorPort);
        motorLeft     = titan.getMotor(Constants.kMotorLeftPort);

        motorRight.setInverted(Constants.kMotorRightInverted);
        motorRear.setInverted(Constants.kMotorRearInverted);
        motorElevator.setInverted(Constants.kMotorElevatorInverted);
        motorLeft.setInverted(Constants.kMotorLeftInverted);

        // Inicializacao dos Encoders
        encoderRight    = titan.getEncoder(Constants.kEncoderRightPort, 1.0);
        encoderRear     = titan.getEncoder(Constants.kEncoderRearPort, 1.0);
        encoderElevator = titan.getEncoder(Constants.kEncoderElevatorPort, 1.0);
        encoderLeft     = titan.getEncoder(Constants.kEncoderLeftPort, 1.0);

        resetEncoders();
        stop();
    }

    @Override
    public void periodic() {
        // Leitura unica dos encoders por ciclo de 20ms para nao sobrecarregar o barramento CAN
        double rawRight    = encoderRight.getRaw()    * (Constants.kEncoderRightInverted    ? -1.0 : 1.0);
        double rawRear     = encoderRear.getRaw()     * (Constants.kEncoderRearInverted     ? -1.0 : 1.0);
        double rawElevator = encoderElevator.getRaw() * (Constants.kEncoderElevatorInverted ? -1.0 : 1.0);
        double rawLeft     = encoderLeft.getRaw()     * (Constants.kEncoderLeftInverted     ? -1.0 : 1.0);

        currentTicksRight    = rawRight - offsetTicksRight;
        currentTicksRear     = rawRear - offsetTicksRear;
        currentTicksElevator = rawElevator - offsetTicksElevator;
        currentTicksLeft     = rawLeft - offsetTicksLeft;

        // Telemetria limpa e organizada no SmartDashboard
        updateDashboard();
    }

    /**
     * Atualiza no SmartDashboard a odometria e distancias percorridas por cada motor.
     */
    public void updateDashboard() {
        // Distancia percorrida por cada motor individualmente (em centimetros)
        SmartDashboard.putNumber("Dist M0 Elevador (cm)", round1(getElevatorDistanceCm()));
        SmartDashboard.putNumber("Dist M1 Direita (cm)",  round1(getRightDistanceCm()));
        SmartDashboard.putNumber("Dist M2 Tras (cm)",     round1(getRearDistanceCm()));
        SmartDashboard.putNumber("Dist M3 Esquerda (cm)", round1(getLeftDistanceCm()));

        // Nomenclatura explicita de Odometria
        SmartDashboard.putNumber("Odometria M0 Elevador (cm)", round1(getElevatorDistanceCm()));
        SmartDashboard.putNumber("Odometria M1 Direita (cm)",  round1(getRightDistanceCm()));
        SmartDashboard.putNumber("Odometria M2 Tras (cm)",     round1(getRearDistanceCm()));
        SmartDashboard.putNumber("Odometria M3 Esquerda (cm)", round1(getLeftDistanceCm()));

        // Pulsos/ticks de cada motor
        SmartDashboard.putNumber("Encoder M0 Elevador",   currentTicksElevator);
        SmartDashboard.putNumber("Encoder M1 Direita",    currentTicksRight);
        SmartDashboard.putNumber("Encoder M2 Tras",       currentTicksRear);
        SmartDashboard.putNumber("Encoder M3 Esquerda",   currentTicksLeft);

        // Potencia aplicada a cada motor
        SmartDashboard.putNumber("Potencia M0 Elevador",  round2(motorElevator.get()));
        SmartDashboard.putNumber("Potencia M1 Direita",   round2(motorRight.get()));
        SmartDashboard.putNumber("Potencia M2 Tras",      round2(motorRear.get()));
        SmartDashboard.putNumber("Potencia M3 Esquerda",  round2(motorLeft.get()));
    }

    /**
     * Aciona diretamente os motores de tracao (M1 Direita, M2 Tras, M3 Esquerda) com potencias individuais.
     * Utilizado para controle de precisao de pulsos em comandos sequenciais.
     * @param powerRight Potencia do motor direito (M1) [-1.0 a 1.0]
     * @param powerRear  Potencia do motor traseiro (M2) [-1.0 a 1.0]
     * @param powerLeft  Potencia do motor esquerdo (M3) [-1.0 a 1.0]
     */
    public void setDriveMotorPowers(double powerRight, double powerRear, double powerLeft) {
        motorRight.set(powerRight);
        motorRear.set(powerRear);
        motorLeft.set(powerLeft);
    }

    /**
     * Aciona exclusivamente um unico motor para teste individual a baixa velocidade.
     * Garante que os outros 3 motores fiquem completamente desligados (0.0).
     * @param motorIndex Indice da porta na Titan [0=M0 Elevador, 1=M1 Direita, 2=M2 Tras, 3=M3 Esquerda]
     * @param power Potencia (-1.0 a 1.0)
     */
    public void runSingleMotorTest(int motorIndex, double power) {
        motorRight.set(motorIndex == Constants.kMotorRightPort ? power : 0.0);
        motorRear.set(motorIndex == Constants.kMotorRearPort ? power : 0.0);
        motorElevator.set(motorIndex == Constants.kMotorElevatorPort ? power : 0.0);
        motorLeft.set(motorIndex == Constants.kMotorLeftPort ? power : 0.0);
    }

    /**
     * Acionamento direto do motor traseiro M2 para diagnostico e teste individual.
     * @param power Potencia (-1.0 a 1.0)
     */
    public void setRearMotorDirect(double power) {
        runSingleMotorTest(Constants.kMotorRearPort, power);
    }

    /**
     * Movimentacao omnidirecional Kiwi (3 rodas a 120 graus):
     * @param vx    Velocidade lateral (positivo = direita, negativo = esquerda)
     * @param vy    Velocidade longitudinal (positivo = frente, negativo = tras)
     * @param omega Velocidade de rotacao (positivo = horario/direita, negativo = anti-horario/esquerda)
     */
    public void drive(double vx, double vy, double omega) {
        // Equacoes cinematicas Kiwi Drive com compensacao angular das rodas dianteiras
        double vRight = vy - (Constants.kLateralFront * vx) - omega;
        double vLeft  = vy + (Constants.kLateralFront * vx) + omega;
        double vRear  = vx - omega;

        // Normalizacao proporcional: impede saturacao mantendo a direcao resultante
        double maxPower = Math.max(Math.abs(vRight), Math.max(Math.abs(vLeft), Math.abs(vRear)));
        if (maxPower > 1.0) {
            vRight /= maxPower;
            vLeft  /= maxPower;
            vRear  /= maxPower;
        }

        // Aplica o limite global de velocidade
        vRight *= Constants.kMaxSpeed;
        vLeft  *= Constants.kMaxSpeed;
        vRear  *= Constants.kMaxSpeed;

        motorRight.set(vRight);
        motorLeft.set(vLeft);
        motorRear.set(vRear);
        // O motor do elevador (M0) permanece inalterado / sem atuacao na tracao
    }

    /**
     * Para todos os motores imediatamente.
     */
    public void stop() {
        motorRight.set(0.0);
        motorLeft.set(0.0);
        motorRear.set(0.0);
        motorElevator.set(0.0);
    }

    /**
     * Zera todos os encoders e a odometria de cada um dos motores.
     * Atualiza imediatamente o SmartDashboard com os valores zerados.
     */
    public void resetEncoders() {
        encoderRight.reset();
        encoderRear.reset();
        encoderElevator.reset();
        encoderLeft.reset();

        double rawRight    = encoderRight.getRaw()    * (Constants.kEncoderRightInverted    ? -1.0 : 1.0);
        double rawRear     = encoderRear.getRaw()     * (Constants.kEncoderRearInverted     ? -1.0 : 1.0);
        double rawElevator = encoderElevator.getRaw() * (Constants.kEncoderElevatorInverted ? -1.0 : 1.0);
        double rawLeft     = encoderLeft.getRaw()     * (Constants.kEncoderLeftInverted     ? -1.0 : 1.0);

        offsetTicksRight    = rawRight;
        offsetTicksRear     = rawRear;
        offsetTicksElevator = rawElevator;
        offsetTicksLeft     = rawLeft;

        currentTicksRight    = 0.0;
        currentTicksRear     = 0.0;
        currentTicksElevator = 0.0;
        currentTicksLeft     = 0.0;

        // Atualiza imediatamente o dashboard com os valores zerados
        updateDashboard();
    }

    // ---- Distancias e Leituras em Centimetros --------------------------------
    public double getRightDistanceCm() {
        return (currentTicksRight / Constants.kEncoderCPR) * Constants.kWheelCircumferenceCm;
    }

    public double getRearDistanceCm() {
        return (currentTicksRear / Constants.kEncoderCPR) * Constants.kWheelCircumferenceCm;
    }

    public double getLeftDistanceCm() {
        return (currentTicksLeft / Constants.kEncoderCPR) * Constants.kWheelCircumferenceCm;
    }

    public double getElevatorDistanceCm() {
        return (currentTicksElevator / Constants.kEncoderCPR) * Constants.kWheelCircumferenceCm;
    }

    public double getRightTicks()    { return currentTicksRight; }
    public double getRearTicks()     { return currentTicksRear; }
    public double getElevatorTicks() { return currentTicksElevator; }
    public double getLeftTicks()     { return currentTicksLeft; }

    // ---- Acesso aos Motores (caso necessario para testes futuros) ------------
    public Titan.Motor getMotorElevator() {
        return motorElevator;
    }

    public Titan.Encoder getEncoderElevator() {
        return encoderElevator;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
