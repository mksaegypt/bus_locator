<?php 
	error_reporting(E_ALL);
	ini_set('display_errors', 1);
	
    define ("DB_HOST", "host");
    define ("DB_NAME", "aches_sebas_test");
    define ("DB_USER", "aches_sebas_test");
    define ("DB_PASS", "your_pass");
    
    
    define ("TABLE_PREFIX", "xta");
    
    //tables:
	define("RUTAS",     TABLE_PREFIX."rutas");
    define("BUSES",     TABLE_PREFIX."buses");
	
	require_once("rb.php");
	
	R::setup('mysql:host=localhost;dbname=s035352e_asyscontrol','s035352e_asys','a23gel67');
?>