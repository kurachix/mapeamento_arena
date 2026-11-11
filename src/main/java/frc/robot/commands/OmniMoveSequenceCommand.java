/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.OmniDriveSubsystem;
import frc.robot.Constants;

/**
 * Comando teleoperado disparado pelo Botao B:
 * Realiza 2 movimentos sequenciais com contagem precisa de pulsos nos motores:
 * Movimento 1: M1: 2095, M2: 0,     M3: 2007
 * Movimento 2: M1: 3705, M2: -2752, M3: 832
 */
public class OmniMoveSequenceCommand extends CommandBase {

    private final OmniDriveSubsystem driveSubsystem;

    private enum State {
        INIT_STEP1,
        RUN_STEP1,
        PAUSE_BETWEEN,
        INIT_STEP2,
        RUN_STEP2,
        FINISHED
    }

    private State currentState = State.INIT_STEP1;

    // Posicoes de referencia de inicio de cada etapa
    private double startTicksRight = 0.0;
    private double startTicksRear  = 0.0;
    private double startTicksLeft  = 0.0;

    // Alvos de pulsos da etapa atual
    private double targetTicksRight = 0.0;
    private double targetTicksRear  = 0.0;
    private double targetTicksLeft  = 0.0;

    // Contador de ciclos de pausa entre os movimentos
    private int pauseCounter = 0;
    private static final int PAUSE_CYCLES = 10; // 10 ciclos * 20ms = 200ms de estabilizacao

    public OmniMoveSequenceCommand(OmniDriveSubsystem driveSubsystem) {
        this.driveSubsystem = driveSubsystem;
        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        currentState = State.INIT_STEP1;
        pauseCounter = 0;
        driveSubsystem.stop();
        SmartDashboard.putString("Sequencia Botao B", "Iniciando Movimento 1");
    }

    @Override
    public void execute() {
        double currentRight = driveSubsystem.getRightTicks();
        double currentRear  = driveSubsystem.getRearTicks();
        double currentLeft  = driveSubsystem.getLeftTicks();

        switch (currentState) {
            case INIT_STEP1:
                startTicksRight = currentRight;
                startTicksRear  = currentRear;
                startTicksLeft  = currentLeft;

                // Alvos do Movimento 1 a partir da posicao atual
                targetTicksRight = startTicksRight + Constants.kMove1TicksRight;
                targetTicksRear  = startTicksRear  + Constants.kMove1TicksRear;
                targetTicksLeft  = startTicksLeft  + Constants.kMove1TicksLeft;

                currentState = State.RUN_STEP1;
                SmartDashboard.putString("Sequencia Botao B", "Executando Movimento 1 (M1: 2095, M2: 0, M3: 2007)");
                break;

            case RUN_STEP1:
                boolean step1Done = controlMotorsToTarget(currentRight, currentRear, currentLeft);
                if (step1Done) {
                    driveSubsystem.stop();
                    pauseCounter = 0;
                    currentState = State.PAUSE_BETWEEN;
                    SmartDashboard.putString("Sequencia Botao B", "Pausa de Transicao...");
                }
                break;

            case PAUSE_BETWEEN:
                driveSubsystem.stop();
                pauseCounter++;
                if (pauseCounter >= PAUSE_CYCLES) {
                    currentState = State.INIT_STEP2;
                }
                break;

            case INIT_STEP2:
                if (Constants.kMove2IsRelative) {
                    // Movimento 2 realiza exatamente a quantidade de pulsos a partir do fim do Movimento 1
                    startTicksRight = currentRight;
                    startTicksRear  = currentRear;
                    startTicksLeft  = currentLeft;

                    targetTicksRight = startTicksRight + Constants.kMove2TicksRight;
                    targetTicksRear  = startTicksRear  + Constants.kMove2TicksRear;
                    targetTicksLeft  = startTicksLeft  + Constants.kMove2TicksLeft;
                } else {
                    // Movimento 2 com pulsos acumulados a partir do ponto de partida da rotina
                    targetTicksRight = startTicksRight + Constants.kMove2TicksRight;
                    targetTicksRear  = startTicksRear  + Constants.kMove2TicksRear;
                    targetTicksLeft  = startTicksLeft  + Constants.kMove2TicksLeft;
                }

                currentState = State.RUN_STEP2;
                SmartDashboard.putString("Sequencia Botao B", "Executando Movimento 2 (M1: 3705, M2: -2752, M3: 832)");
                break;

            case RUN_STEP2:
                boolean step2Done = controlMotorsToTarget(currentRight, currentRear, currentLeft);
                if (step2Done) {
                    driveSubsystem.stop();
                    currentState = State.FINISHED;
                    SmartDashboard.putString("Sequencia Botao B", "Concluido com Sucesso");
                }
                break;

            case FINISHED:
                driveSubsystem.stop();
                break;
        }
    }

    /**
     * Aplica controle proporcional nos motores de tracao ate atingirem a contagem de pulsos.
     * Retorna true quando todos os 3 motores estiverem dentro da margem de tolerancia.
     */
    private boolean controlMotorsToTarget(double currentRight, double currentRear, double currentLeft) {
        double errorRight = targetTicksRight - currentRight;
        double errorRear  = targetTicksRear  - currentRear;
        double errorLeft  = targetTicksLeft  - currentLeft;

        boolean rightReached = Math.abs(errorRight) <= Constants.kMovePulseTolerance;
        boolean rearReached  = Math.abs(errorRear)  <= Constants.kMovePulseTolerance;
        boolean leftReached  = Math.abs(errorLeft)  <= Constants.kMovePulseTolerance;

        double powerRight = calculateMotorPower(errorRight, rightReached);
        double powerRear  = calculateMotorPower(errorRear,  rearReached);
        double powerLeft  = calculateMotorPower(errorLeft,  leftReached);

        driveSubsystem.setDriveMotorPowers(powerRight, powerRear, powerLeft);

        // Telemetria detalhada de acompanhamento no SmartDashboard
        SmartDashboard.putNumber("Seq Erro M1 Direita",  Math.round(errorRight));
        SmartDashboard.putNumber("Seq Erro M2 Tras",     Math.round(errorRear));
        SmartDashboard.putNumber("Seq Erro M3 Esquerda", Math.round(errorLeft));

        return rightReached && rearReached && leftReached;
    }

    /**
     * Calcula a potencia proporcional para o motor com limites de velocidade e compensacao de atrito estatico.
     */
    private double calculateMotorPower(double error, boolean reached) {
        if (reached) {
            return 0.0;
        }

        double power = error * Constants.kMoveKp;

        // Saturacao na velocidade maxima
        if (power > Constants.kMoveMaxSpeed) {
            power = Constants.kMoveMaxSpeed;
        } else if (power < -Constants.kMoveMaxSpeed) {
            power = -Constants.kMoveMaxSpeed;
        }

        // Potencia minima para evitar travamento mecânico por atrito estático
        if (Math.abs(power) < Constants.kMoveMinSpeed) {
            power = Math.copySign(Constants.kMoveMinSpeed, error);
        }

        return power;
    }

    @Override
    public void end(boolean interrupted) {
        driveSubsystem.stop();
        if (interrupted) {
            SmartDashboard.putString("Sequencia Botao B", "Interrompido");
        }
    }

    @Override
    public boolean isFinished() {
        return currentState == State.FINISHED;
    }
}
