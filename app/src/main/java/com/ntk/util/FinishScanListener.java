package com.ntk.util;

import java.util.ArrayList;

/*
 * Copyright 2014 Carlos Ferreyra.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public interface FinishScanListener {
	
	
	/**
	 * Interface called when the scan method finishes. Network operations should not execute on UI thread  
	 * @param  ArrayList of {@link ClientScanResult}
	 */
	
	public void onFinishScan(ArrayList<ClientScanResult> clients);
	
	public void onDeviceConnect(String device_ip);

}
