package io.github.abhinandanjoshii.hygiene;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name="check")
public class HygieneMojo extends AbstractMojo{

    @Override
    public void execute(){
        getLog().info("HygieneMojo running");
    }

}
