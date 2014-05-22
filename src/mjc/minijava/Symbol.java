package mjc.minijava;

public abstract class Symbol {
	private String id;

	public Symbol(String i) {
		id = i;
	}

	public String getIdentifier() {
		return id;
	}
}
