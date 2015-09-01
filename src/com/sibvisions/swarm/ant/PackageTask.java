package com.sibvisions.swarm.ant;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.wildfly.swarm.tools.BuildTool;

import com.sibvisions.swarm.Resolver;
import com.sibvisions.util.FileSearch;
import com.sibvisions.util.io.NonClosingInputStream;
import com.sibvisions.util.type.CommonUtil;
import com.sibvisions.util.type.FileUtil;

public class PackageTask extends Task
{
    /** the output file. */
    private String outputFile;
    
    /** the library directory. */
    private String libDir;
    
    /** the war file. */
    private String war;
    
    /** the backup path. */
    private String backupPath;
    
    public void execute()
    {
        try
        {
            //important for shrinkwrap (otherwise classloading won't work with ant)
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            
            BuildTool tool = new BuildTool();

            File fiDepsDir = new File(libDir);
            File fiWar = new File(war);
    
            FileSearch fs = new FileSearch(); 
            fs.search(fiDepsDir, false);
            
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
            
            Resolver resolver = new Resolver(tool);
            
            if (backupPath != null)
            {
                resolver.setBackupPath(backupPath);
            }
            
            tool.artifactResolvingHelper(resolver);
            
            tool.projectArtifact("com.sibvisions.demos", "swarm", "0.1", "war", fiWar);
            tool.build(FileUtil.removeExtension(FileUtil.getName(outputFile)), Paths.get(FileUtil.getDirectory(outputFile)));
        }
        catch (Throwable th)
        {   
            throw new BuildException(th);
        }
    }

    public void setWar(String war)
    {
        this.war = war;
    }
    
    public void setLibDir(String libDir)
    {
        this.libDir = libDir;
    }
    
    public void setOutputFile(String outputFile)
    {
        this.outputFile = outputFile;
    }
    
    public void setBackupPath(String backupPath)
    {
        this.backupPath = backupPath;
    }
    
}   // PackageTask
