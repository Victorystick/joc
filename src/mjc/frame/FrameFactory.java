package mjc.frame;

import java.util.List;
import mjc.ir.Temp;
import mjc.ir.Label;
import underscore.Mapper;

abstract public class FrameFactory implements Mapper<Temp, String> {
	abstract public int getWordSize();

	// Creates non-escaping arguments.
	abstract public Frame create(Label name, int narg);

	abstract public Frame create(Label name, boolean[] args);

	abstract public List<Temp> getRegisters();
	abstract public List<Temp> getAvailable();

	public Frame create(String name, int params) {
		return create(Label.create(name), params);
	}

	public Frame create(String name, boolean[] args) {
		return create(Label.create(name), args);
	}
}
