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

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

import uk.ac.raubach.lit.pojo.*;

/**
 * @author Sebastian Raubach
 */
public class LauncherComponent extends Composite
{
	private String       current;
	private List<Button> buttons = new ArrayList<>();

	private List<OnConfigChangedListener> listeners = new ArrayList<>();

	public LauncherComponent(Composite parent, int style)
	{
		super(parent, style);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
	}

	public void init(Config config)
	{
		for (Control control : this.getChildren())
		{
			if (control != null && !control.isDisposed())
				control.dispose();
		}
		buttons.clear();

		List<String> setups = config.getSetups();

		for (String setup : setups)
		{
			final String s = setup;
			Button button = new Button(this, SWT.RADIO);
			button.setText(setup);
			button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			button.addListener(SWT.Selection, e -> {
				current = s;
			});
			buttons.add(button);
		}

		Composite buttonComp = new Composite(this, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonComp.setLayout(layout);

		Button add = new Button(buttonComp, SWT.NONE);
		add.setText("+");
		add.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		add.addListener(SWT.Selection, e -> {
			InputDialog dialog = new InputDialog(getShell(), "New setup", "Please enter the new setup name", null, null);
			if (dialog.open() == Window.OK)
			{
				String setup = dialog.getValue();

				config.addSetup(setup);

				init(config);

				notifyListeners();
			}
		});
		Button del = new Button(buttonComp, SWT.NONE);
		del.setText("-");
		del.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		del.addListener(SWT.Selection, e -> {
			for (Button button : buttons)
			{
				if (!button.isDisposed())
				{
					if (button.getSelection())
					{
						String setup = button.getText();

						config.removeSetup(setup);

						init(config);

						notifyListeners();

						break;
					}
				}
			}
		});

		getShell().layout(true, true);
	}

	private void notifyListeners()
	{
		listeners.forEach(OnConfigChangedListener::onConfigChanged);
	}

	public void addOnConfigChangedListener(OnConfigChangedListener listener)
	{
		listeners.add(listener);
	}

	public void removeOnConfigChangedListener(OnConfigChangedListener listener)
	{
		listeners.remove(listener);
	}

	public String getSetup()
	{
		return current;
	}

	public interface OnConfigChangedListener
	{
		void onConfigChanged();
	}
}
