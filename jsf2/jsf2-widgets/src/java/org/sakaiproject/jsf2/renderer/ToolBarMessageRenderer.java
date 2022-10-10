/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.jsf2.renderer;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

import org.sakaiproject.jsf2.util.RendererUtil;


public class ToolBarMessageRenderer extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.write("<h1>");
  }

  /**
   * @param context FacesContext for the request we are processing
   * @param component UIComponent to be rendered
   * @exception IOException if an input/output error occurs while rendering
   * @exception NullPointerException if <code>context</code> or <code>component</code> is null
   */
  public void encodeEnd(FacesContext context, UIComponent component) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    String txt = (String) RendererUtil.getAttribute(context, component, "value");
    if (txt != null)
    {
      writer.write(txt);
    }

    writer.write("</h1>");
  }
}

