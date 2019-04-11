package com.ibagroup.wf.intelia.core.templates;

//import java.io.InputStream;
//import org.apache.commons.lang.StringUtils;
//import org.apache.velocity.exception.ResourceNotFoundException;
//import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
//import org.apache.velocity.util.ExceptionUtils;

public class VelocityClassPathResourceLoader {/*extends ClasspathResourceLoader {

  @Override
  public InputStream getResourceStream(String name) throws ResourceNotFoundException {
    InputStream result = null;
    if (StringUtils.isEmpty(name)) {
      throw new ResourceNotFoundException("No template name provided");
    } else {
      try {
        result = VelocityClassPathResourceLoader.class.getResourceAsStream(name);
      } catch (Exception arg3) {
        throw (ResourceNotFoundException) ExceptionUtils.createWithCause(
            ResourceNotFoundException.class, "problem with template: " + name, arg3);
      }

      if (result == null) {
        String msg = "ClasspathResourceLoader Error: cannot find resource " + name;
        throw new ResourceNotFoundException(msg);
      } else {
        return result;
      }
    }
  }
*/
}
