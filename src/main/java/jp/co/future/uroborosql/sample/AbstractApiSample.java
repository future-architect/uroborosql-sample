package jp.co.future.uroborosql.sample;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractApiSample {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger("uroborosql-sample");

	public AbstractApiSample() {
		super();
	}

	/**
	 * Read TSV file data and convert to List Object.
	 *
	 * @param filePath TSV file path.
	 * @return Data List
	 */
	protected Stream<Map<String, Object>> getDataByFile(final Path filePath) {
		try {
			List<String> lines = Files.readAllLines(filePath);
			String[] header = lines.get(0).split("\\t");
			return lines.stream()
					.skip(1)
					.map(s -> s.split("\\t"))
					.map(data -> IntStream.range(0, header.length)
							.<Map<String, Object>> collect(HashMap::new, (row, i) -> row.put(header[i], data[i]),
									Map::putAll));
		} catch (IOException e) {
			e.printStackTrace();
			throw new UncheckedIOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected String toS(final Object obj) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			return map.entrySet().stream()
					.map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining(",", "{", "}"));
		} else {
			return obj.toString();
		}
	}

	protected void log(final String format, final Object... arguments) {
		log.info(format, arguments);
	}

}