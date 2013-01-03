/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat JeMoni, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jemoni.carbon;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class VirtualCarbonServerRule extends VirtualCarbonServer implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() { @Override public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                close();
            }
        }};
    }
}
