package org.jimple.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class TestCaseParser {
    private static final Pattern BREAK_LINE_PAT = Pattern.compile("<-+->[\\n\\r]*");

    public List<TestCase> parse(final URL resourceUrl) throws IOException {
        final String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        final String[] parts = BREAK_LINE_PAT.split(content);
        final int caseCount = (parts.length + 1) / 2;
        final List<TestCase> result = new ArrayList<>(caseCount);

        for (int i = 0; i < caseCount; i++) {
            final int index = i * 2;
            if (StringUtils.isNotBlank(parts[index])) {
                result.add(new TestCase(parts[index], (index + 1) < parts.length ? parts[index + 1] : StringUtils.EMPTY));
            }
        }

        return Collections.unmodifiableList(result);
    }
}
