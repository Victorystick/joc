package mjc.frame;

import mjc.ir.IRNode;

abstract public class Access {
	abstract public IRNode value(IRNode framePtr);
}
