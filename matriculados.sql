-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 27-05-2025 a las 23:29:52
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `matriculados`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estudianteweb`
--

CREATE TABLE `estudianteweb` (
  `codiEstdWeb` int(11) NOT NULL,
  `dniEstdWeb` varchar(50) DEFAULT NULL,
  `appaEstdWeb` varchar(50) DEFAULT NULL,
  `apmaEstdWeb` varchar(50) DEFAULT NULL,
  `nombEstdWeb` varchar(50) DEFAULT NULL,
  `fechNaciEstdWeb` date DEFAULT NULL,
  `logiEstd` varchar(100) DEFAULT NULL,
  `passEstd` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `estudianteweb`
--

INSERT INTO `estudianteweb` (`codiEstdWeb`, `dniEstdWeb`, `appaEstdWeb`, `apmaEstdWeb`, `nombEstdWeb`, `fechNaciEstdWeb`, `logiEstd`, `passEstd`) VALUES
(1, '12345678', 'Gomezñññ', 'Perez', 'Luis', '2000-05-06', 'lgomez', '123456'),
(2, '87654321', 'Lopez', 'Martinez', 'Ana', '1999-11-25', 'alopez', 'mypassword456'),
(3, '11223344', 'Fernandez', 'Sanchez', 'Carlos', '2001-07-15', 'cfernandez', 'securepass789'),
(28, '60427062', 'ROMERO', 'Estrada', 'fernnado', '2025-05-07', 'nando', 'd85d4cd');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `estudianteweb`
--
ALTER TABLE `estudianteweb`
  ADD PRIMARY KEY (`codiEstdWeb`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `estudianteweb`
--
ALTER TABLE `estudianteweb`
  MODIFY `codiEstdWeb` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
