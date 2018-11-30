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

import org.eclipse.swt.graphics.*;

import java.io.*;
import java.util.*;

import uk.ac.raubach.lit.ui.*;

/**
 * @author Sebastian Raubach
 */
public class Resources
{
	/**
	 * Disposes all {@link Image}s that were loaded during execution (if they haven't already been disposed)
	 */
	public static void disposeResources()
	{
		Images.disposeAll();
	}

	public static class Images
	{
		private static Map<String, Image> CACHE = new HashMap<>();

		public static Image LOGO = getImage("img/logo.png");

		public static Image getImage(String path, boolean cache)
		{
			Image result = cache ? CACHE.get(path) : null;

			if (result == null)
			{
				if (Lit.WITHIN_JAR)
				{
					InputStream stream = Resources.class.getClassLoader().getResourceAsStream(path);
					if (stream != null)
					{
						result = new Image(null, stream);
					}
				}
				else
				{
					result = new Image(null, path);
				}

				if (result != null && cache)
					CACHE.put(path, result);
			}

			return result;
		}

		public static Image getImage(String path)
		{
			return getImage(path, true);
		}

		public static void disposeAll()
		{
			CACHE.values()
				 .stream()
				 .filter(i -> !i.isDisposed())
				 .forEach(Image::dispose);
		}
	}
}
