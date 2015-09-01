package com.sibvisions.swarm;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.wildfly.swarm.tools.BuildTool;

import com.sibvisions.util.FileSearch;
import com.sibvisions.util.io.NonClosingInputStream;
import com.sibvisions.util.type.CommonUtil;
import com.sibvisions.util.type.FileUtil;

public class PackageTest
{
    @Test
    public void testCreate() throws Exception
    {
        //be sure that all dependencies are available in deps dir (e.g. start ant task)
        
        BuildTool tool = new BuildTool();
        
        File fiDepsDir = new File(new File("").getAbsolutePath(), "/build/swarmlibs/default");
        File fiWar = new File(new File("").getAbsolutePath(), "DemoERP.war");

        FileSearch fs = new FileSearch();
        fs.search(fiDepsDir, true, "*.jar");
        
        for (String sFile : fs.getFoundFiles())
        {
            File fiJar = new File(sFile);
            
            String sGroupId = null;
            String sArtifactId = null;
            String sVersion = null;

            //try to read maven properties from archive
            FileInputStream fis = null;
            ZipInputStream zis = null;
            
            try
            {
                fis = new FileInputStream(fiJar);
                zis = new ZipInputStream(fis);

                ZipEntry zeCurrent;
                
                while ((zeCurrent = zis.getNextEntry()) != null)
                {
                    if (!zeCurrent.isDirectory() 
                        && FileUtil.like(zeCurrent.getName(), "META-INF/**/pom.properties")
                        && !FileUtil.like(zeCurrent.getName(), "META-INF/**/org.jboss.modules/**/pom.properties"))
                    {
                        Properties prop = new Properties();
                        prop.load(new NonClosingInputStream(zis));
                        
                        sGroupId = prop.getProperty("groupId");
                        sArtifactId = prop.getProperty("artifactId");
                        sVersion = prop.getProperty("version");
                    }
                }
            }
            finally
            {
                CommonUtil.close(fis, zis);
            }
            
            if (sGroupId != null)
            {
                tool.dependency("compile", sGroupId, sArtifactId, sVersion, "jar", null, fiJar);
            }
        }
        
        tool.artifactResolvingHelper(new Resolver(tool));
        
        tool.projectArtifact("com.sibvisions.demos", "swarm", "0.1", "war", fiWar);
        tool.build("swarm-0.1", Paths.get(new File("").getAbsolutePath()));
    }
}
