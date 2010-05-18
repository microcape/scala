/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.apis.content

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import _root_.android.app.Activity
import _root_.android.content.Context
import _root_.android.content.res.Resources
import _root_.android.os.Bundle
import _root_.android.widget.TextView


/**
 * Demonstration of loading resources.
 * 
 * <p>
 * Each context has a resources object that you can access.  Additionally,
 * the Context class (an Activity is a Context) has a getString convenience
 * method getString() that looks up a string resource.
 *
 * @see StyledText for more depth about using styled text, both with getString()
 *                 and in the layout xml files.
 */
class ResourcesSample extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // See res/any/layout/resources.xml for this view layout definition.
    setContentView(R.layout.resources)

    var tv: TextView = null
    var cs: CharSequence = null
    var str: String = null

    // ====== Using the Context.getString() convenience method ===========

    // Using the getString() conevenience method, retrieve a string
    // resource that hapepns to have style information.  Note the use of
    // CharSequence instead of String so we don't lose the style info.
    cs = getText(R.string.styled_text)
    tv = findViewById(R.id.styled_text).asInstanceOf[TextView]
    tv setText cs

    // Use the same resource, but convert it to a string, which causes it
    // to lose the style information.
    str = getString(R.string.styled_text)
    tv = findViewById(R.id.plain_text).asInstanceOf[TextView]
    tv setText str

    // ====== Using the Resources object =================================
        
    // You might need to do this if your code is not in an activity.
    // For example View has a protected mContext field you can use.
    // In this case it's just 'this' since Activity is a context.
    val context: Context = this

    // Get the Resources object from our context
    val res = context.getResources

    // Get the string resource, like above.
    cs = res.getText(R.string.styled_text)
    tv = findViewById(R.id.res1).asInstanceOf[TextView]
    tv setText cs

    // Note that the Resources class has methods like getColor(),
    // getDimen(), getDrawable() because themes are stored in resources.
    // You can use them, but you might want to take a look at the view
    // examples to see how to make custom widgets.

  }
}
