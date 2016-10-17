<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:oai="http://www.openarchives.org/OAI/2.0/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    
    <xsl:param name="provider_uri" select="()"/>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="oai:OAI-PMH">
        <xsl:variable name="sets">
            <xsl:variable name="uri" select="concat(($provider_uri,oai:request)[1],'?verb=ListSets')"/>
            <xsl:choose>
                <xsl:when test="doc-available($uri)">
                    <xsl:sequence select="doc($uri)"/>
                    <!--<xsl:message>DBG: ListsSets[<xsl:value-of select="$uri"/>] available</xsl:message>-->
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="()"/>
                    <xsl:message>WRN: ListsSets[<xsl:value-of select="$uri"/>] not available</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--<xsl:message>DBG: ListsSets[<xsl:value-of select="count($sets//oai:set)"/>]</xsl:message>-->
        <xsl:copy>
            <xsl:apply-templates select="node() | @*">
                <xsl:with-param name="sets" select="$sets" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="oai:setSpec">
        <xsl:param name="sets" tunnel="yes"/>
        <xsl:copy>
            <xsl:variable name="set" select="$sets//oai:set[oai:setSpec=current()]/oai:setName"/>
            <xsl:if test="normalize-space($set)!=''">
                <xsl:attribute name="setName" select="$set"/>
            </xsl:if>
            <xsl:apply-templates select="@*">
                <xsl:with-param name="sets" select="$sets" tunnel="yes"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="sets" select="$sets" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
