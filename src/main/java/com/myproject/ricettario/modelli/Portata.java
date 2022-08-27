package com.myproject.ricettario.modelli;

public enum Portata {
	ANTIPASTO("ANTIPASTO"), PRIMO("PRIMO"), SECONDO("SECONDO"), DOLCE("DOLCE"), CONTORNO("CONTORNO");

	public static Portata of(String stringa) {
		if (stringa.equals("ANTIPASTO"))
			return ANTIPASTO;
		if (stringa.equals("PRIMO"))
			return PRIMO;
		if (stringa.equals("SECONDO"))
			return SECONDO;
		if (stringa.equals("DOLCE"))
			return DOLCE;
		if (stringa.equals("CONTORNO"))
			return CONTORNO;
		else
			return null;
	}

	private String nomeFriendly;

	Portata(String nomeFriendly) {
		this.nomeFriendly = nomeFriendly;
	}

	public String toString() {

		return this.nomeFriendly;
	}
}
