/*
 * Copyright 2016-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.biville.florent.sproccompiler.export;

import net.biville.florent.sproccompiler.export.messages.DsvExportError;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DsvWriterTest
{

    @Test
    public void writes_csv_records()
    {
        StringWriter writer = new StringWriter();
        try ( DsvWriter dsvWriter = new DsvWriter( Arrays.asList( "first header", "second header" ), writer ) )
        {
            dsvWriter.write(
                    Stream.of( "haha_this is", "so much_fun" ),
                    this::parseRow,
                    (error) -> fail("Unexpected export error: " + error)
            );
            String result = writer.toString();
            assertThat( result ).isEqualTo(
                    "\"first header\",\"second header\"\n" + "\"haha\",\"this is\"\n" + "\"so much\",\"fun\"\n" );
        }
    }

    private Stream<Either<DsvExportError,String>> parseRow(String input)
    {
        return Arrays.stream( input.split( "_" )).map( Either::right );
    }
}
