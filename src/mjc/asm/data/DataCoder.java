package mjc.asm.data;

import java.util.List;

public interface DataCoder {
	public void generateData(List<DataInstruction> in);
	public String doneData();
}