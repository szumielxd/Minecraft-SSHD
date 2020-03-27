package com.ryanmichela.sshd;

import java.util.Optional;

import com.ryanmichela.sshd.SshdPlugin;

public class PermissionUtil 
{
    public static Optional<String> GetCredential(String username, String credential)
    {
        String Default = SshdPlugin.instance.getConfig().getString("Credentials.$default." + credential);
        String cred = SshdPlugin.instance.getConfig().getString("Credentials." + username + "." + credential, Default);

        if (cred == null)
            return Optional.empty();

        else if (cred.isEmpty())
            return Optional.empty();
            
        else
            return Optional.of(cred);
    }
};