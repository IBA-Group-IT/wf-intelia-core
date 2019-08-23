package com.ibagroup.wf.intelia.core.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerTemplate extends TextTemplate {

	private Template template;

	public FreeMarkerTemplate(InputStream inputStream) throws IOException {
		this(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
	}

	public FreeMarkerTemplate(InputStream inputStream, String encoding) throws IOException {
		this(IOUtils.toString(inputStream, encoding));
	}

	public FreeMarkerTemplate(String templateText) throws IOException {
		super(templateText);
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		StringReader reader = new StringReader(templateText);
		this.template = new Template(this.templateName, reader, cfg);
	}

	@Override
	public String compile() throws IOException, TemplateException {
		Writer writer = new StringWriter();
		template.process(scopes, writer);
		writer.flush();
		return writer.toString();
	}

	@Override
	public void compileAndWrite(Writer writer) throws IOException, TemplateException {
		template.process(scopes, writer);
		writer.flush();
		writer.close();
	}
}
