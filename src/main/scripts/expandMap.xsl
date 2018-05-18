<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:csv="https://vlo.clarin.eu/ns/csv" 
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="xs math"
    version="3.0">
    
    <xsl:output method="text" encoding="UTF-8"/>
    
    <xsl:variable name="NL" select="system-property('line.separator')"/>
    
    <xsl:param name="map" select="'map.csv'"/>
    <xsl:param name="cr" select="'https://centres.clarin.eu'"/>
    
    <xsl:variable name="OAIPMHEndpoint" select="json-to-xml(unparsed-text(concat($cr,'/api/model/OAIPMHEndpoint')))"/>
    <xsl:variable name="Centre" select="json-to-xml(unparsed-text(concat($cr,'/api/model/Centre')))"/>
    <xsl:variable name="Consortium" select="json-to-xml(unparsed-text(concat($cr,'/api/model/Consortium')))"/>
    
    <xsl:function name="csv:getTokens" as="xs:string+">
        <xsl:param name="str" as="xs:string"/>
        <xsl:analyze-string select="concat($str, ',')" regex='(("[^"]*")+|[^,]*),'>
            <xsl:matching-substring>
                <xsl:sequence select='replace(regex-group(1), "^""|""$|("")""", "$1")'/>
            </xsl:matching-substring>
        </xsl:analyze-string>
    </xsl:function>
    
    <xsl:template name="main">
        <xsl:variable name="rows">
            <xsl:choose>
                <xsl:when test="unparsed-text-available($map)">
                    <xsl:variable name="tab" select="unparsed-text($map)"/>
                    <xsl:variable name="lines" select="tokenize($tab, '(\r)?\n')" as="xs:string+"/>
                    <xsl:variable name="elemNames" select="csv:getTokens($lines[1])" as="xs:string+"/>
                    <xsl:message >DBG: CSV headers[<xsl:value-of select="string-join($elemNames,',')"/>]</xsl:message>
                    <xsl:for-each select="$lines[position() &gt; 1][normalize-space(.) != '']">
                        <xsl:variable name="line" select="position()"/>
                        <r l="{$line}">
                            <xsl:variable name="lineItems" select="csv:getTokens(.)" as="xs:string+"/>
                            <xsl:if test="count($lineItems)!=count($elemNames)">
                                <xsl:message terminate="yes">ERR: CSV[<xsl:value-of select="$map"/>] line[<xsl:value-of select="$line"/>] has [<xsl:value-of select="count($lineItems)"/>] cells, but the header indicates that [<xsl:value-of select="count($elemNames)"/>] cells are expected!</xsl:message>
                            </xsl:if>
                            <xsl:for-each select="$elemNames">
                                <xsl:variable name="col" select="position()"/>
                                <c n="{.}">
                                    <xsl:value-of select="$lineItems[$col]"/>
                                </c>
                            </xsl:for-each>
                        </r>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">ERR: couldn't load CSV[<xsl:value-of select="$map"/>]!</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="count($rows/r) = 0">
            <xsl:message terminate="yes">ERR: no data loaded from CSV[<xsl:value-of select="$map"/>]!</xsl:message>
        </xsl:if>
        <xsl:text>endpointUrl,directoryName,centreName,nationalProject</xsl:text>
        <xsl:value-of select="$NL"/>
        <xsl:for-each select="$rows/r">
            <xsl:variable name="ep" select="c[@n='endpointUrl']"/>
            <xsl:variable name="c"  select="$OAIPMHEndpoint//fn:map[fn:string[@key='uri']=$ep]/fn:number[@key='centre']"/>
            <xsl:variable name="ce" select="$Centre//fn:map[fn:number[@key='pk']=$c]/fn:map[@key='fields']"/>
            <xsl:variable name="co" select="$Centre//fn:map[fn:number[@key='pk']=$c]/fn:map[@key='fields']/fn:number[@key='consortium']"/>
            <xsl:variable name="np" select="$Consortium//fn:map[fn:number[@key='pk']=$co]/fn:map[@key='fields']"/>
            <!--<xsl:message>row[<xsl:value-of select="position()"/>] endpoint[<xsl:value-of select="$ep"/>] centre[<xsl:value-of select="$ce/fn:string[@key='name']"/>] national project[<xsl:value-of select="$np/fn:string[@key='name']"/> (<xsl:value-of select="$np/fn:string[@key='country_code']"/>)]</xsl:message>-->
            <xsl:value-of select="$ep"/>
            <xsl:text>,</xsl:text>
            <xsl:value-of select="c[@n='directoryName']"/>
            <xsl:text>,</xsl:text>
            <xsl:if test="normalize-space($ce/fn:string[@key='name'])!=''">
                <xsl:text>"</xsl:text>
                <xsl:value-of select="replace($ce/fn:string[@key='name'],'&quot;','&quot;&quot;')"/>
                <xsl:text>"</xsl:text>
            </xsl:if>
            <xsl:text>,</xsl:text>
            <xsl:if test="normalize-space($np/fn:string[@key='name'])!=''">
                <xsl:text>"</xsl:text>
                <xsl:value-of select="replace($np/fn:string[@key='name'],'&quot;','&quot;&quot;')"/>
                <!--<xsl:if test="normalize-space($np/fn:string[@key='country_code'])!=''">
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="replace($np/fn:string[@key='country_code'],'&quot;','&quot;&quot;')"/>
                    <xsl:text>)</xsl:text>
                </xsl:if>-->
                <xsl:text>"</xsl:text>
            </xsl:if>
            <xsl:value-of select="$NL"/>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>