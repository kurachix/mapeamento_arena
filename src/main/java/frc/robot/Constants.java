/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

public final class Constants {

    private Constants() {}

    // ID CAN da controladora Titan Quad
    public static final int kTitanCanId = 42;

    // =========================================================================
    // Portas dos Motores na Titan Quad:
    // M0 - Elevador / Garra
    // M1 - Direita
    // M2 - Tras
    // M3 - Esquerda
    // =========================================================================
    public static final int kMotorElevatorPort = 0; // M0 - Elevador / Garra
    public static final int kMotorRightPort    = 1; // M1 - Direita
    public static final int kMotorRearPort     = 2; // M2 - Tras
    public static final int kMotorLeftPort     = 3; // M3 - Esquerda

    // Portas dos encoders correspondentes na Titan Quad
    public static final int kEncoderElevatorPort = 0;
    public static final int kEncoderRightPort    = 1;
    public static final int kEncoderRearPort     = 2;
    public static final int kEncoderLeftPort     = 3;

    // Inversao dos motores
    public static final boolean kMotorElevatorInverted = false;
    public static final boolean kMotorRightInverted    = true;
    public static final boolean kMotorRearInverted     = true;
    public static final boolean kMotorLeftInverted     = false;

    // Inversao dos encoders (para sentido positivo corresponder a rotacao positiva)
    public static final boolean kEncoderElevatorInverted = false;
    public static final boolean kEncoderRightInverted    = true;
    public static final boolean kEncoderRearInverted     = true;
    public static final boolean kEncoderLeftInverted     = false;

    // =========================================================================
    // Parametros Cinematicos do Chassi Kiwi (3 rodas omni a 120 graus)
    // =========================================================================
    // Fator de compensacao lateral para as rodas dianteiras (cos 60 = 0.50)
    public static final double kLateralFront = 0.50;

    // Limite de velocidade maxima no teleoperado (0.0 a 1.0)
    public static final double kMaxSpeed = 0.80;

    // Especificacoes das rodas Studica Omni 100mm e encoders Maverick
    public static final double kEncoderCPR = 1464.0;
    public static final double kWheelDiameterCm = 10.16;
    public static final double kWheelCircumferenceCm = Math.PI * kWheelDiameterCm;

    // =========================================================================
    // Configuracao do Joystick
    // =========================================================================
    public static final int kDriverControllerPort = 0;
    public static final double kJoystickDeadband = 0.12;

    // Velocidade lenta e segura para teste individual dos motores (0.0 a 1.0)
    public static final double kTestMotorSpeed = 0.25;

    // =========================================================================
    // Parametros dos 2 Movimentos por Pulsos (Botao B)
    // =========================================================================
    // Movimento 1:
    public static final double kMove1TicksRight = 2095.0; // M1: 2095 pulsos
    public static final double kMove1TicksRear  = 0.0;    // M2: 0 pulsos
    public static final double kMove1TicksLeft  = 2007.0; // M3: 2007 pulsos

    // Movimento 2:
    public static final double kMove2TicksRight = 3705.0;  // M1: 3705 pulsos
    public static final double kMove2TicksRear  = -2752.0; // M2: -2752 pulsos
    public static final double kMove2TicksLeft  = 832.0;   // M3: 832 pulsos

    // Tolerancia para considerar o motor no alvo (em pulsos)
    public static final double kMovePulseTolerance = 15.0;

    // Ganhos e limites de velocidade para controle de posicao por pulsos
    public static final double kMoveMaxSpeed = 0.40;
    public static final double kMoveMinSpeed = 0.12;
    public static final double kMoveKp       = 0.0012;

    // Se true: Movimento 2 realiza essa quantidade a partir do fim do Movimento 1.
    // Se false: Movimento 2 considera os pulsos acumulados desde o inicio da rotina.
    public static final boolean kMove2IsRelative = true;
}
