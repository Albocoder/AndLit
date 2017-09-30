<?php
	$EMAIL_FOLDER_PATH = "received_emails";
	$response = array('code' => "1",'msg' => "Not all the data was provided!");
	if( isset($_POST['email']) && !empty($_POST['email']) && isset($_POST['message']) && !empty($_POST['message'])){
		$emailArray = array('name'=>$_POST['name'], 'email'=>$_POST['email'], 'message'=>$_POST['message']);

		$fname = date('d_M_Y').".json";
		$fname = $EMAIL_FOLDER_PATH."/".$fname;

		$jsonString = "";
		if(file_exists($fname))
			$jsonString = file_get_contents($fname) or (print(json_encode(array('code' => "2",'msg' => "Can't open DB to read!"))) and die());		
		$json_arr = json_decode($jsonString, true);
		
		if (is_null($json_arr))
			$json_arr = array();

		array_push($json_arr,$emailArray);
		$handle = fopen($fname,"w") or (print(json_encode(array('code' => "2",'msg' => "Can't open DB to write!"))) and die());
		fwrite($handle,json_encode($json_arr));
		fclose($handle);
		
		$response = array('code' => "0",'msg' => "Success!");
	}
	echo json_encode($response);
?>