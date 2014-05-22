package mjc.minijava;

import mjc.*;

class Validator {
	private final CompilerOptions options;
	private final String enableGenerics = "\n\t\t(enable generics with -g)";

	public Validator(CompilerOptions opts) {
		options = opts;
	}

	public void generics(MiniJavaParser.ClassDeclContext cls) {
		if (!options.generics) {
			if (cls.generics() != null) {
				Errors.error(cls.generics().start,
					String.format(
						"attempted to define generic class '%s'.%s",
						cls.identifier().getText(),
						enableGenerics));
			}

			MiniJavaParser.ExtensionContext ext = cls.extension();
			if (ext != null && ext.generics() != null) {
				Errors.error(ext.start,
					String.format(
						"class '%s' attempted to extend generic class '%s'.%s",
						cls.identifier().getText(),
						ext.identifier().getText(),
						enableGenerics));
			}
		}
	}

	public void genericField(MiniJavaParser.VarDeclContext var) {
		if (!options.generics) {
			if (var.generics() != null) {
				Errors.error(var.generics().start,
					String.format(
						"attempted to define generic field '%s'.%s",
						var.identifier().getText(),
						enableGenerics));
			}
		}
	}

	public void main(MiniJavaParser.MainContext main) {
		String id = main.identifier().get(1).getText();

		if (!id.equals("main")) {
			Errors.error(main.start,
				String.format(
					"Main class requires method called 'main', found '%s'.",
					id));
		}
	}
}
