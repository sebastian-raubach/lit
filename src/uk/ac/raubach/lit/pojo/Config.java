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

package uk.ac.raubach.lit.pojo;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
public class Config
{
	private List<String>  setups   = new ArrayList<>();
	private List<Program> programs = new ArrayList<>();

	public List<String> getSetups()
	{
		return setups;
	}

	public void addSetup(String setup)
	{
		setups.add(setup);
	}

	public void removeSetup(String setup)
	{
		setups.remove(setup);

		programs.stream()
				.filter(p -> p.getSetups().contains(setup))
				.forEach(p -> p.getSetups().remove(setup));
	}

	public List<Program> getPrograms()
	{
		return programs;
	}

	public void addProgram(Program program)
	{
		programs.add(program);
	}

	public void removeProgram(Program program)
	{
		programs.remove(program);
	}

	@Override
	public String toString()
	{
		return "Config{" +
			"programs=" + programs +
			'}';
	}

	public static class Program
	{
		private String       program = "";
		private String       path    = "";
		private List<String> setups  = new ArrayList<>();

		public String getProgram()
		{
			return program;
		}

		public Program setProgram(String program)
		{
			this.program = program;
			return this;
		}

		public String getPath()
		{
			return path;
		}

		public Program setPath(String path)
		{
			this.path = path;
			return this;
		}

		public List<String> getSetups()
		{
			return setups;
		}

		public Program setSetups(List<String> setups)
		{
			this.setups = setups;
			return this;
		}

		@Override
		public String toString()
		{
			return "Program{" +
				"program='" + program + '\'' +
				", path='" + path + '\'' +
				", setups=" + setups +
				'}';
		}
	}
}
