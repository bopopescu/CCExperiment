package org.apache.cassandra.cql;
public class Relation
{
    private EntityType entityType = EntityType.COLUMN;
    private Term entity;
    private RelationType relationType;
    private Term value;
    public Relation(Term entity, String type, Term value)
    {
        if (entity.getText().toUpperCase().equals("KEY"))
            this.entityType = EntityType.KEY;
        this.entity = entity;
        this.relationType = RelationType.forString(type);
        this.value = value;
    }
    public boolean isKey()
    {
        return entityType.equals(EntityType.KEY);
    }
    public boolean isColumn()
    {
        return entityType.equals(EntityType.COLUMN);
    }
    public RelationType operator()
    {
        return relationType;
    }
    public Term getEntity()
    {
        return entity;
    }
    public Term getValue()
    {
        return value;
    }
    public String toString()
    {
        return String.format("Relation(%s, %s,nnn %s)", entity, relationType, value);
    }
}
enum EntityType
{
    KEY, COLUMN;
}
enum RelationType
{
    EQ, LT, LTE, GTE, GT;
    public static RelationType forString(String s)
    {
        if (s.equals("="))
            return EQ;
        else if (s.equals("<"))
            return LT;
        else if (s.equals("<="))
            return LTE;
        else if (s.equals(">="))
            return GTE;
        else if (s.equals(">"))
            return GT;
        return null;
    }
}
