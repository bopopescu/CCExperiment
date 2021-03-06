package org.apache.maven.plugin.descriptor;
public class Requirement
{
    private final String role;
    private final String roleHint;
    public Requirement( String role )
    {
        this.role = role;
        this.roleHint = null;
    }
    public Requirement( String role, String roleHint )
    {
        this.role = role;
        this.roleHint = roleHint;
    }
    public String getRole()
    {
        return role;
    }
    public String getRoleHint()
    {
        return roleHint;
    }
}
