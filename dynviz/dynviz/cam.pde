
class HandyCam {
	public Camera cam;
	public Vector3D pos;
	public Vector3D unit;
	
	HandyCam (float x, float y, float z) {
		this.pos = new Vector3D(x, y, z);
		this.unit = this.pos.unit();
	}
	
	public void update() { 
		if (this.cam != null) {
			this.cam.jump(this.pos.x, this.pos.y, this.pos.z);
		}
	}
	
	public void crane(float units) {
		Vector3D offset = this.unit.times(units);
		this.pos.add(offset);
		this.update();
	}
	
	public void craneTo(Vector3D v) {
		this.pos.set(v);
		this.update();
	}
}
