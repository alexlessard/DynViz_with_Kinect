public class Vector3D {

	public Vector3D(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}

	public Vector3D() {
		x = 0.0F;
		y = 0.0F;
		z = 0.0F;
	}

	public Vector3D(Vector3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public final float z() {
		return z;
	}

	public final float y() {
		return y;
	}

	public final float x() {
		return x;
	}

	public final void setX(float X) {
		x = X;
	}
 
		public final void setY(float Y) {
		y = Y;
	}

	public final void setZ(float Z) {
		z = Z;
	}

	public final void set(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}

	public final void set(Vector3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public final Vector3D add(Vector3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
		return this;
	}

	public final void subtract(Vector3D p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
	}

	public final void add(float a, float b, float c) {
		x += a;
		y += b;
		z += c;
	}
        
        public final void divide(float a, float b, float c)
        {
            x /= a;
            y /= b;
            z /= c;
        }
        
        public final void divide(float a)
        {
            x /= a;
            y /= a;
            z /= a;
        }

	public final Vector3D plus(Vector3D p) {
		return new Vector3D(x + p.x, y + p.y, z + p.z);
	}

	public final Vector3D times(float f) {
		return new Vector3D(x * f, y * f, z * f);
	}

	public final Vector3D over(float f) {
		return new Vector3D(x / f, y / f, z / f);
	}

	public final Vector3D minus(Vector3D p) {
		return new Vector3D(x - p.x, y - p.y, z - p.z);
	}

	public final Vector3D multiplyBy(float f) {
		x *= f;
		y *= f;
		z *= f;
		return this;
	}
	
	public final Vector3D negate() {
		return new Vector3D(-x, -y, -z);
	}

	public final float distanceTo(Vector3D p) {
		float dx = x - p.x;
		float dy = y - p.y;
		float dz = z - p.z;
		return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public final float distanceTo(float x, float y, float z) {
		float dx = this.x - x;
		float dy = this.y - y;
		float dz = this.z - z;
		return 1.0F / fastInverseSqrt(dx * dx + dy * dy + dz * dz);
	}

	public final float dot(Vector3D p) {
		return x * p.x + y * p.y + z * p.z;
	}

	public final float length() {
		return (float)Math.sqrt(x * x + y * y + z * z);
	}

	public final Vector3D unit() {
		float l = length();
		return l != 0.0F ? over(l) : new Vector3D();
	}

	public final void clear() {
		x = 0.0F;
		y = 0.0F;
		z = 0.0F;
	}
        
        public final Vector3D getNormalized()
        {
          return this.over(this.length());
        }
        
        public final void normalize()
        {
            this.divide(this.length());
        }

	public final String toString() {
		return new String("(" + x + ", " + y + ", " + z + ")");
	}
	
	public final Vector3D cross(Vector3D p) {
		return new Vector3D(y * p.z - z * p.y, x * p.z - z * p.x, x * p.y - y * p.x);
	}
	
	public final float angleWith(Vector3D b) {
		float aDotb = this.dot(b);
		float ab = this.length() * b.length();
                if (ab == 0)
                    return 0.0f;
		return acos(aDotb / ab);
	}
        
        public final float projectionOn(Vector3D b)
        {
                float aDotb = this.dot(b);
		float ab = this.length() * b.length();
                if (ab == 0)
                  return 0;
                return this.length()*aDotb/ab;
        }
        

	float x;
	float y;
	float z;
}

public static float fastInverseSqrt(float x) {
	float half = 0.5F * x;
	int i = Float.floatToIntBits(x);
	i = 0x5f375a86 - (i >> 1);
	x = Float.intBitsToFloat(i);
	return x * (1.5F - half * x * x);
}
