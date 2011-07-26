class BlindThread implements Runnable {
	public boolean cancel = false;
	public String command;
	
	public BlindThread(String inCommand) {
		command = inCommand;
	}
	
	public void run() {
		try {
			final Process trace = Runtime.getRuntime().exec(command);
			final BufferedReader br = new BufferedReader(new InputStreamReader(trace.getInputStream()));
			String line;
			while((line = br.readLine()) != null && cancel == false) {
				println(line);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
