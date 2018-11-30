/*
 * Copyright 2018 Sebastian Raubach <sebastian@raubach.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.raubach.lit.util;

import com.google.gson.*;

import java.io.*;
import java.nio.file.*;

import uk.ac.raubach.lit.pojo.*;

/**
 * @author Sebastian Raubach
 */
public class ConfigIO
{
	private static final File FILE = new File(new File(System.getProperty("user.home"), ".baz"), "lit.json");

	public static Config read()
		throws IOException
	{
		Gson gson = new Gson();

		try (InputStreamReader is = new InputStreamReader(new FileInputStream(FILE)))
		{
			return gson.fromJson(is, Config.class);
		}
	}

	public static void write(Config config)
		throws IOException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(config, Config.class);

		Files.write(FILE.toPath(), json.getBytes());
	}
}
