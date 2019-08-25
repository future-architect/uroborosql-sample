package jp.co.future.uroborosql.sample;

/**
 * uroboroSQL Sample Application
 *
 * @author H.Sugimoto
 */
public class Main {
	public static void main(final String... args) throws Exception {
		// SQLFile Sample
		SqlFileApiSample sqlFileApiSample = new SqlFileApiSample();
		sqlFileApiSample.run();

		// Entity Sample
		EntityApiSample entityApiSample = new EntityApiSample();
		entityApiSample.run();
	}
}
