/**
 *
 */
package jp.co.future.uroborosql.sample.type;

/**
 * 性別を表す列挙体
 *
 * @author H.Sugimoto
 */
public enum Gender {
	Male("M"), Female("F"), Other("O");

	private final String label;

	private Gender(final String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

}
