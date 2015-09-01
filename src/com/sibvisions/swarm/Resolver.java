package com.sibvisions.swarm;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.filter.FilterHelper;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;

import com.sibvisions.swarm.url.ByteURLStreamHandler;


public class Resolver implements ArtifactResolvingHelper
{
    private BuildTool tool;

    private Ivy ivy;
    
    public Resolver(BuildTool pTool)
    {
        tool = pTool;
        
        IvySettings settings = new IvySettings();
        
        try
        {
            settings.load(new File(new File("").getAbsolutePath(), "ivysettings.xml"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        ivy = Ivy.newInstance(settings);
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec pSpec) throws Exception
    {
        for (ArtifactSpec each : tool.dependencies()) 
        {
            if (each.file == null)
            {
                continue;
            }
            
            if (pSpec.artifactId != null && !pSpec.artifactId.equals(each.artifactId)) {
                continue;
            }

            if (pSpec.version != null && !pSpec.version.equals(each.version)) {
                continue;
            }

            if (pSpec.packaging != null && !pSpec.packaging.equals(each.packaging)) {
                continue;
            }

            if (pSpec.classifier != null && !pSpec.classifier.equals(each.classifier)) {
                continue;
            }

            return each;
        }
        
        //try to resolve via ivy (in-memory file)

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("<ivy-module version=\"2.0\">".getBytes());
        baos.write("<info organisation=\"com.sibvisions\" module=\"swarm\"/>".getBytes());
        baos.write("<dependencies>".getBytes());
        baos.write(("<dependency org=\"" + pSpec.groupId + "\" name=\"" + pSpec.artifactId +"\" rev=\"" + pSpec.version +"\" transitive=\"false\" />").getBytes());
        baos.write("</dependencies>".getBytes());    
        baos.write("</ivy-module>".getBytes());    
        
        ResolveOptions resoptions = new ResolveOptions();
        resoptions.setArtifactFilter(FilterHelper.getArtifactTypeFilter(new String[] {"jar", "bundle"}));
        
        ResolveReport resreport = ivy.resolve(new URL("intern", "ivy-resolve", 0, "", new ByteURLStreamHandler(baos.toByteArray())), resoptions);
        
        if (!resreport.hasError())
        {
            ModuleDescriptor desc = resreport.getModuleDescriptor();
            
            ModuleRevisionId id = desc.getModuleRevisionId();
            
            RetrieveOptions retoptions = new RetrieveOptions();
            //retoptions.setArtifactFilter(FilterHelper.getArtifactTypeFilter(new String[] {"jar", "bundle"}));
            retoptions.setDestArtifactPattern(new File(new File("").getAbsolutePath(), "libs/ivy").getAbsolutePath() +"/[conf]/[artifact]-[revision].[ext]");
            
            RetrieveReport retreport = ivy.retrieve(id, retoptions);
            
            if (retreport.getRetrievedFiles().size() > 0)
            {
                return new ArtifactSpec("compile", pSpec.groupId, pSpec.artifactId, pSpec.version, "jar", null, 
                                        (File)((List<?>)retreport.getRetrievedFiles()).get(0));
            }
        }

        return null;
    }
    
}
