package mjc.x64;

import mjc.frame.Access;
import mjc.ir.*;

class InRegister extends Access {
	Temp temp;

	public static InRegister create() {
		return create(Temp.create());
	}

	public static InRegister create(Temp t) {
		return new InRegister(t);
	}

	InRegister(Temp t) {
		temp = t;
	}

	public IRNode value(IRNode ignore) {
		return temp;
	}
}
