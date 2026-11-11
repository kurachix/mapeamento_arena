/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

import frc.robot.subsystems.OmniDriveSubsystem;
import frc.robot.commands.OmniTeleopCommand;
import frc.robot.commands.OmniMoveSequenceCommand;

public class RobotContainer {

    // Subsistema de tracao omnidirecional
    private final OmniDriveSubsystem driveSubsystem = new OmniDriveSubsystem();

    // Controle do piloto
    private final XboxController driverController = new XboxController(Constants.kDriverControllerPort);

    public RobotContainer() {
        // Define a movimentacao teleoperada por joystick como comando padrao do subsistema
        driveSubsystem.setDefaultCommand(
            new OmniTeleopCommand(driveSubsystem, driverController)
        );

        configureButtonBindings();
        configureDashboardBindings();
    }

    /**
     * Configuracao dos botoes fisicos do controle via Command-Based.
     */
    private void configureButtonBindings() {
        // Botao A do controle (Xbox A): Reseta a odometria e os encoders de cada um dos motores
        new JoystickButton(driverController, XboxController.Button.kA.value)
            .whenPressed(new InstantCommand(driveSubsystem::resetEncoders));

        // Botao B do controle (Xbox B): Executa os 2 movimentos com a contagem exata de pulsos
        new JoystickButton(driverController, XboxController.Button.kB.value)
            .whenPressed(new OmniMoveSequenceCommand(driveSubsystem));
    }

    /**
     * Botoes e comandos adicionados ao SmartDashboard para controle e testes.
     */
    private void configureDashboardBindings() {
        // Botoes de utilidade geral no SmartDashboard
        SmartDashboard.putData("Zerar Odometria", new InstantCommand(driveSubsystem::resetEncoders));
        SmartDashboard.putData("Parada Emergencia", new InstantCommand(driveSubsystem::stop, driveSubsystem));

        // Botao para acionar a sequencia de 2 movimentos pelo Dashboard
        SmartDashboard.putData("Executar Sequencia 2 Movimentos (Botao B)",
            new OmniMoveSequenceCommand(driveSubsystem));

        // Botoes de teste individual de cada motor via SmartDashboard (baixa velocidade)
        SmartDashboard.putData("Testar M1 Direita (Quadrado)",
            new InstantCommand(() -> driveSubsystem.runSingleMotorTest(Constants.kMotorRightPort, Constants.kTestMotorSpeed), driveSubsystem));

        SmartDashboard.putData("Testar M2 Tras (Triangulo)",
            new InstantCommand(() -> driveSubsystem.runSingleMotorTest(Constants.kMotorRearPort, Constants.kTestMotorSpeed), driveSubsystem));

        SmartDashboard.putData("Testar M0 Elevador (Bola)",
            new InstantCommand(() -> driveSubsystem.runSingleMotorTest(Constants.kMotorElevatorPort, Constants.kTestMotorSpeed), driveSubsystem));

        SmartDashboard.putData("Testar M3 Esquerda (X)",
            new InstantCommand(() -> driveSubsystem.runSingleMotorTest(Constants.kMotorLeftPort, Constants.kTestMotorSpeed), driveSubsystem));

        SmartDashboard.putData("Parar Teste",
            new InstantCommand(driveSubsystem::stop, driveSubsystem));
    }

    /**
     * Como o robô opera no modo teleoperado, nao ha comando autonomo fixo configurado.
     */
    public Command getAutonomousCommand() {
        return null;
    }

    public OmniDriveSubsystem getDriveSubsystem() {
        return driveSubsystem;
    }
}
