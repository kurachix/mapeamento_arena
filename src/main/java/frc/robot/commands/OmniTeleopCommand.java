/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.OmniDriveSubsystem;
import frc.robot.Constants;

public class OmniTeleopCommand extends CommandBase {

    private final OmniDriveSubsystem driveSubsystem;
    private final XboxController controller;

    private boolean isDirectInput = false;
    private boolean modeDetected = false;

    public OmniTeleopCommand(OmniDriveSubsystem driveSubsystem, XboxController controller) {
        this.driveSubsystem = driveSubsystem;
        this.controller = controller;
        addRequirements(driveSubsystem);
    }

    @Override
    public void execute() {
        // Atualiza a deteccao do modo do controle (DirectInput vs XInput)
        detectControllerMode();

        // =====================================================================
        // Leitura dos botoes fisicos do controle (DirectInput e XInput)
        // =====================================================================
        boolean btnSquare;
        boolean btnTriangle;
        boolean btnCircle;
        boolean btnA;

        if (isDirectInput) {
            btnSquare   = controller.getRawButton(1); // Esquerda (Quadrado no Logitech D / PS)
            btnA        = controller.getRawButton(2); // Baixo (X no Logitech D / A no Xbox D)
            btnCircle   = controller.getRawButton(3); // Direita (Bola no Logitech D / B no Xbox D)
            btnTriangle = controller.getRawButton(4); // Cima (Triangulo no Logitech D / Y no Xbox D)
        } else {
            btnA        = controller.getAButton();    // Baixo (Botao A no Xbox / XInput)
            btnCircle   = controller.getBButton();    // Direita (Botao B no Xbox)
            btnSquare   = controller.getXButton();    // Esquerda (Botao X no Xbox)
            btnTriangle = controller.getYButton();    // Cima (Botao Y no Xbox)
        }

        // =====================================================================
        // Botao A: Reset da odometria de cada um dos motores
        // Zera os encoders e atualiza imediatamente os valores zerados no dashboard
        // =====================================================================
        if (btnA) {
            driveSubsystem.resetEncoders();
            SmartDashboard.putString("Status Odometria", "Odometria Zerada (Botao A)");
        }

        // =====================================================================
        // Teste Individual de Motores por Botao Fisico
        // M1 - Direita (Quadrado / X)
        // M2 - Tras    (Triangulo / Y)
        // Obs: O Botao A e utilizado para Zerar Odometria e o Botao B para a Sequencia
        // =====================================================================
        if (btnSquare) {
            driveSubsystem.runSingleMotorTest(Constants.kMotorRightPort, Constants.kTestMotorSpeed);
            SmartDashboard.putString("Modo Teste Motor", "ATIVO: M1 Direita (Quadrado)");
            return;
        } else if (btnTriangle) {
            driveSubsystem.runSingleMotorTest(Constants.kMotorRearPort, Constants.kTestMotorSpeed);
            SmartDashboard.putString("Modo Teste Motor", "ATIVO: M2 Tras (Triangulo)");
            return;
        }

        SmartDashboard.putString("Modo Teste Motor", "Nenhum (Movimentacao Normal)");

        // =====================================================================
        // Movimentacao Normal por Joysticks (Modo Teleoperado)
        // =====================================================================
        // Joystick Esquerdo: Translacao
        // Eixo Y: Frente (+) e Tras (-) [WPILib inverte o Y por padrao]
        double rawY = -controller.getY(Hand.kLeft);

        // Eixo X: Direita (+) e Esquerda (-) [Translacao lateral / Strafe]
        double rawX = controller.getX(Hand.kLeft);

        // Rotacao no proprio eixo: Stick Direito ou Botoes Bumpers (LB / RB)
        double rawOmega = getRightStickX();

        if (controller.getBumper(Hand.kRight)) {
            rawOmega = 0.50; // Giro horario via RB
        } else if (controller.getBumper(Hand.kLeft)) {
            rawOmega = -0.50; // Giro anti-horario via LB
        }

        // Aplicacao de zona morta para evitar ruidos de repouso nos potenciometros
        double vy = applyDeadband(rawY);
        double vx = applyDeadband(rawX);
        double omega = applyDeadband(rawOmega);

        // Se todos os comandos estiverem na zona morta, para os motores
        if (vx == 0.0 && vy == 0.0 && omega == 0.0) {
            driveSubsystem.stop();
        } else {
            driveSubsystem.drive(vx, vy, omega);
        }

        // Telemetria no SmartDashboard para diagnostico em tempo real
        SmartDashboard.putNumber("Joystick Vx (Lateral)", round2(vx));
        SmartDashboard.putNumber("Joystick Vy (Frente)",  round2(vy));
        SmartDashboard.putNumber("Joystick Omega (Giro)", round2(omega));
        SmartDashboard.putString("Modo Controle", isDirectInput ? "DirectInput (D)" : "XInput (X)");

        // Indicador visual didatico do comportamento do motor traseiro no chassi Kiwi
        if (Math.abs(vx) < 0.01 && Math.abs(omega) < 0.01) {
            SmartDashboard.putString("Status Motor M2 Tras", "Repouso (Normal em Frente/Tras)");
        } else {
            SmartDashboard.putString("Status Motor M2 Tras", "Ativo (Translacao/Giro)");
        }
    }

    private void detectControllerMode() {
        if (!modeDetected) {
            double initialAxis4 = controller.getRawAxis(4);
            // Em repouso, se o eixo 4 estiver desconectado ou travado em -1.0, e DirectInput
            if (controller.getAxisCount() <= 4 || initialAxis4 < -0.6) {
                isDirectInput = true;
            } else {
                isDirectInput = false;
            }
            modeDetected = true;
        }

        // Deteccao dinamica: no XInput, o eixo 2 e o gatilho esquerdo (0.0 a 1.0, nunca negativo).
        // Se o eixo 2 ficar negativo, o controle certamente esta em DirectInput.
        double axis2 = controller.getRawAxis(2);
        if (axis2 < -0.20) {
            isDirectInput = true;
        }
    }

    /**
     * Leitura do eixo X do stick direito com deteccao confiavel de modo.
     */
    private double getRightStickX() {
        if (isDirectInput) {
            return controller.getRawAxis(2);
        } else {
            return controller.getX(Hand.kRight);
        }
    }

    private double applyDeadband(double value) {
        if (Math.abs(value) < Constants.kJoystickDeadband) {
            return 0.0;
        }
        return (value - Math.copySign(Constants.kJoystickDeadband, value)) / (1.0 - Constants.kJoystickDeadband);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public void end(boolean interrupted) {
        driveSubsystem.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}

