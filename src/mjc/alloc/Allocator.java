package mjc.alloc;

import mjc.asm.InstructionSequence;
import mjc.ir.Temp;
import underscore.Mapper;

public interface Allocator {
	InstructionSequence allocate( InstructionSequence is );
	Mapper<Temp, Temp> getMapper();
}
