<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:oai="http://www.openarchives.org/OAI/2.0/"
    xmlns:sx="java:nl.mpi.tla.saxon" exclude-result-prefixes="sx">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    
    <xsl:param name="provider_uri" select="()"/>
    <xsl:param name="config" select="()"/>
    
    <xsl:template match="/" priority="1">
        <xsl:copy>
            <xsl:apply-templates>
                <xsl:with-param name="filter" tunnel="yes" select="$config//provider[@url=$provider_uri]/filter"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="oai:record">
        <xsl:param name="filter" tunnel="yes"/>
        <xsl:variable name="rec" select="."/>
        <xsl:comment>DBG: provider_uri[<xsl:value-of select="$provider_uri"/>] filter[<xsl:value-of select="$filter"/>]</xsl:comment>
        <xsl:choose>
            <xsl:when test="normalize-space($filter)!=''">
                <xsl:choose>
                    <xsl:when test="sx:evaluate($rec, $filter, $filter)">
                        <xsl:next-match/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>INF: skipped record[<xsl:value-of select="oai:header/oai:identifier"/>]</xsl:message> 
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
        
</xsl:stylesheet>