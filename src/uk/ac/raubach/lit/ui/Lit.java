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

package uk.ac.raubach.lit.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.io.*;

import uk.ac.raubach.lit.pojo.*;
import uk.ac.raubach.lit.util.*;

/**
 * @author Sebastian Raubach
 */
public class Lit extends Dialog
{
	public static boolean WITHIN_JAR;

	private Config            config;
	private LauncherComponent launch;

	public Lit(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	public int open()
	{
		try
		{
			config = ConfigIO.read();
		}
		catch (IOException e)
		{
			config = new Config();
		}

		return super.open();
	}

	@Override
	protected void okPressed()
	{
		try
		{
			ConfigIO.write(config);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String setup = launch.getSetup();

		if (setup != null)
		{
			config.getPrograms().stream()
				  .filter(p -> p.getSetups().contains(setup))
				  .forEach(this::start);
		}

		super.okPressed();

		System.exit(0);
	}

	@Override
	protected void cancelPressed()
	{
		super.cancelPressed();

		System.exit(0);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());

		TableComponent table = new TableComponent(container, SWT.NONE);
		table.init(config);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group setups = new Group(container, SWT.NONE);
		setups.setText("Setups");
		setups.setLayout(new GridLayout());
		setups.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		launch = new LauncherComponent(setups, SWT.NONE);
		launch.init(config);
		launch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		launch.addOnConfigChangedListener(() -> table.init(config));

		return container;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Lit");
		newShell.setImage(Resources.Images.LOGO);
		newShell.setMinimumSize(400, 300);
	}

	@Override
	protected int getShellStyle()
	{
		return super.getShellStyle();
	}

	@Override
	protected boolean isResizable()
	{
		return true;
	}

	@Override
	public boolean close()
	{
		System.exit(0);

		return super.close();
	}

	public static void main(String[] args)
	{
		WITHIN_JAR = !Lit.class.getResource(Lit.class.getSimpleName() + ".class").toString().startsWith("file");

		new Lit(null).open();
	}

	private void start(Config.Program program)
	{
		try
		{
			Thread.sleep(100);
			String path = program.getPath();
			if (path.endsWith(".bat"))
			{
				File file = new File(path);
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", file.getName());
				pb.directory(file.getParentFile());
				pb.start();
			}
			else
			{
				new ProcessBuilder(path).start();
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
