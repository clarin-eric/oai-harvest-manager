<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.clarin.eu/cmd/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:defns="http://www.openarchives.org/OAI/2.0/"
    xmlns:olac="http://www.language-archives.org/OLAC/1.0/"
    xmlns:olac11="http://www.language-archives.org/OLAC/1.1/"
    xsi:schemaLocation="    http://purl.org/dc/elements/1.1/    http://www.language-archives.org/OLAC/1.0/dc.xsd    http://purl.org/dc/terms/    http://www.language-archives.org/OLAC/1.0/dcterms.xsd    http://www.language-archives.org/OLAC/1.0/    http://www.language-archives.org/OLAC/1.0/olac.xsd    http://www.language-archives.org/OLAC/1.0/ http://www.language-archives.org/OLAC/1.0/third-party/software.xsd ">

    <!-- run on Ubuntu with: saxonb-xslt -ext:on -it main ~/svn/clarin/metadata/trunk/toolkit/xslt/olac2cmdi.xsl  -->

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:key name="iso-lookup" match="lang" use="sil"/>

    <!--
	This parameter can be used to specify path to the iso xml
	file.
      -->
    <xsl:param name="iso_xml_path"/>
    <xsl:variable name="lang-top" select="document(concat($iso_xml_path,'sil_to_iso6393.xml'))/languages"/>

    <!--
	Name of the provider the record was harvested from (or empty
	if name is not known).
      -->
    <xsl:param name="provider_name"/>
    

    <xsl:template match="/">
        <CMD CMDVersion="1.1"
            xsi:schemaLocation="http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd">
            <Header>
                <MdCreator>olac2cmdi.xsl</MdCreator>
                <MdCreationDate>
                    <xsl:variable name="date">
                        <xsl:value-of select="/defns:OAI-PMH/defns:GetRecord[1]/defns:record[1]/defns:header[1]/defns:datestamp[1]"/>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="contains($date,'T')">
                            <xsl:value-of select="substring-before($date,'T')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$date"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </MdCreationDate>
                <MdSelfLink>
                    <xsl:value-of select="/defns:OAI-PMH/defns:GetRecord[1]/defns:record[1]/defns:header[1]/defns:identifier[1]"/>
                </MdSelfLink>
                <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>
                <MdCollectionDisplayName>
                    <xsl:value-of select="$provider_name"/>
                </MdCollectionDisplayName>
            </Header>
            <Resources>
                <ResourceProxyList>
                    <xsl:apply-templates select="//dc:identifier" mode="preprocess"/>
                </ResourceProxyList>
                <JournalFileProxyList/>
                <ResourceRelationList/>
            </Resources>
            <Components>
                <OLAC-DcmiTerms>

                    <xsl:apply-templates select="//dcterms:abstract"/>
                    <xsl:apply-templates select="//dcterms:accessRights"/>
                    <xsl:apply-templates select="//dcterms:accrualMethod"/>
                    <xsl:apply-templates select="//dcterms:accrualPeriodicity"/>
                    <xsl:apply-templates select="//dcterms:accrualPolicy"/>
                    <xsl:apply-templates select="//dcterms:alternative"/>
                    <xsl:apply-templates select="//dcterms:audience"/>
                    <xsl:apply-templates select="//dcterms:available"/>
                    <xsl:apply-templates select="//dcterms:bibliographicCitation"/>
                    <xsl:apply-templates select="//dcterms:conformsTo"/>

                    <xsl:apply-templates select="//dc:contributor"/>
                    <xsl:apply-templates select="//dc:coverage"/>

                    <xsl:apply-templates select="//dcterms:created"/>

                    <xsl:apply-templates select="//dc:creator"/>
                    <xsl:apply-templates select="//dc:date"/>

                    <xsl:apply-templates select="//dcterms:dateAccepted"/>
                    <xsl:apply-templates select="//dcterms:dateCopyrighted"/>
                    <xsl:apply-templates select="//dcterms:dateSubmitted"/>

                    <xsl:apply-templates select="//dc:description"/>

                    <xsl:apply-templates select="//dcterms:educationLevel"/>
                    <xsl:apply-templates select="//dcterms:extent"/>

                    <xsl:apply-templates select="//dc:format"/>

                    <xsl:apply-templates select="//dcterms:hasFormat"/>
                    <xsl:apply-templates select="//dcterms:hasPart"/>
                    <xsl:apply-templates select="//dcterms:hasVersion"/>

                    <xsl:apply-templates select="//dc:identifier"/>

                    <xsl:apply-templates select="//dc:instructionalMethod"/>

                    <xsl:apply-templates select="//dcterms:isFormatOf"/>
                    <xsl:apply-templates select="//dcterms:isPartOf"/>
                    <xsl:apply-templates select="//dcterms:isReferencedBy"/>
                    <xsl:apply-templates select="//dcterms:isReplacedBy"/>
                    <xsl:apply-templates select="//dcterms:isRequiredBy"/>
                    <xsl:apply-templates select="//dcterms:issued"/>
                    <xsl:apply-templates select="//dcterms:isVersionOf"/>

                    <xsl:apply-templates select="//dc:language"/>

                    <xsl:apply-templates select="//dcterms:license"/>
                    <xsl:apply-templates select="//dcterms:mediator"/>
                    <xsl:apply-templates select="//dcterms:medium"/>
                    <xsl:apply-templates select="//dcterms:modified"/>
                    <xsl:apply-templates select="//dcterms:provenance"/>

                    <xsl:apply-templates select="//dc:publisher"/>

                    <xsl:apply-templates select="//dcterms:references"/>

                    <xsl:apply-templates select="//dc:relation"/>

                    <xsl:apply-templates select="//dcterms:replaces"/>
                    <xsl:apply-templates select="//dcterms:requires"/>

                    <xsl:apply-templates select="//dc:rights"/>

                    <xsl:apply-templates select="//dcterms:rightsHolder"/>

                    <xsl:apply-templates select="//dc:source"/>

                    <xsl:apply-templates select="//dcterms:spatial"/>

                    <xsl:apply-templates select="//dc:subject"/>

                    <xsl:apply-templates select="//dcterms:tableOfContents"/>
                    <xsl:apply-templates select="//dcterms:temporal"/>

                    <xsl:apply-templates select="//dc:title"/>
                    <xsl:apply-templates select="//dc:type"/>

                    <xsl:apply-templates select="//dcterms:valid"/>

                </OLAC-DcmiTerms>
            </Components>
        </CMD>
    </xsl:template>

    <xsl:template match="dc:contributor">
        <contributor>
            <xsl:if test="@xsi:type='olac:role'">
                <xsl:if test="@*:code">
                    <xsl:attribute name="olac-role">
                        <!-- note: namespace wildcard necessary to match with both OLAC 1.0 and 1.1 -->
                        <xsl:value-of select="@*:code"/>
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>
            <xsl:value-of select="."/>
        </contributor>
    </xsl:template>

    <xsl:template match="dc:description">
        <description>
            <xsl:apply-templates select="./@xml:lang"/>
            <xsl:apply-templates select="@xsi:type"/>
            <xsl:value-of select="."/>
        </description>
    </xsl:template>

    <xsl:template match="dc:language[@xsi:type='olac:language']" priority="3">
        <language>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-language">
                    <!-- can be enabled when there is a 1-to-1 mapping in sil_to_iso6393.xml           -->
                    <xsl:choose>
                        <xsl:when test="contains(@*:code, 'x-sil-')">
                            <xsl:apply-templates select="$lang-top">
                                <xsl:with-param name="curr-label" select="."/>
                            </xsl:apply-templates>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@*:code"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </language>
    </xsl:template>

    <xsl:template match="languages">
        <xsl:param name="curr-label"/>
        <xsl:variable name="silcode">
            <xsl:value-of select="lower-case(replace($curr-label/@*:code, 'x-sil-', ''))"/>
        </xsl:variable>
        <xsl:value-of select="key('iso-lookup', $silcode)/iso"/>
    </xsl:template>


    <xsl:template match="dc:identifier" mode="preprocess">
  
        <xsl:if test="contains(., 'http://') or contains(., 'urn:nbn') or contains(., 'hdl:')">
        <ResourceProxy>
            <xsl:attribute name="id"><xsl:value-of select="generate-id()"/></xsl:attribute>
            <ResourceType>Resource</ResourceType>
            <ResourceRef><xsl:value-of select="."/></ResourceRef>
        </ResourceProxy>
        </xsl:if>
     
    </xsl:template>


    <xsl:template match="dc:subject[@xsi:type='olac:language']" priority="3">
        <subject>
            <!-- can be enabled when there is a 1-to-1 mapping in sil_to_iso6393.xml           -->
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-language">
                    <xsl:choose>
                        <xsl:when test="contains(@*:code, 'x-sil-')">
                            <xsl:apply-templates select="$lang-top">
                                <xsl:with-param name="curr-label" select="."/>
                            </xsl:apply-templates>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@*:code"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>

    <xsl:template match="//dc:subject[@xsi:type='olac:linguistic-field']" priority="3">
        <subject>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-linguistic-field">
                    <xsl:value-of select="@*:code"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>

    <xsl:template match="//dc:subject[@xsi:type='olac:discourse-type']" priority="3">
        <subject>
            <xsl:attribute name="olac-discourse-type">
                <xsl:value-of select="@*:code"/>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>


    <xsl:template match="//dc:subject" priority="1">
        <subject>
            <xsl:apply-templates select="./@xml:lang"/>
            <xsl:apply-templates select="@xsi:type"/>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>


    <xsl:template match="//dc:title">
        <title>
            <xsl:apply-templates select="./@xml:lang"/>
            <xsl:apply-templates select="@xsi:type"/>
            <xsl:value-of select="."/>
        </title>
    </xsl:template>


    <xsl:template match="//dc:type[@xsi:type='olac:discourse-type']" priority="2">
        <type>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-discourse-type">
                    <xsl:value-of select="@*:code"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </type>
    </xsl:template>


    <xsl:template match="//dc:type[@xsi:type='olac:linguistic-type']" priority="2">
        <type>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-linguistic-type">
                    <xsl:value-of select="@*:code"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </type>
    </xsl:template>


    <xsl:template match="//dc:type" priority="1">
        <type>
            <xsl:apply-templates select="@xsi:type"/>
            <xsl:value-of select="."/>
        </type>
    </xsl:template>

    <xsl:template match="@xml:lang">
        <xsl:attribute name="xml:lang">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="@xsi:type">
        <xsl:variable name="attval">
            <xsl:value-of select="."/>
        </xsl:variable>
        <xsl:if test="contains($attval, 'dcterms:')">
            <xsl:variable name="attclean">
                <xsl:value-of select="replace($attval, 'dcterms:','')"/>
            </xsl:variable>
            <xsl:attribute name="dcterms-type">
                <xsl:value-of select="$attclean"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!--  general DC  template -->
    <xsl:template match="dc:*">
        <xsl:variable name="tagname">
            <xsl:value-of select="local-name()"/>
        </xsl:variable>
        <xsl:element name="{$tagname}">
            <xsl:apply-templates select="@xsi:type"/>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

    <!--  general DC terms template -->
    <xsl:template match="dcterms:*">
        <xsl:variable name="tagname">
            <xsl:value-of select="local-name()"/>
        </xsl:variable>

        <xsl:element name="{$tagname}">
            <xsl:apply-templates select="@xsi:type"/>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>



    <xsl:template name="main">
        <xsl:for-each
            select="collection('file:////home/dietuyt/olac?select=*.xml;recurse=yes;on-error=ignore')">
            <xsl:result-document href="{document-uri(.)}.cmdi">
                <xsl:apply-templates select="."/>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>


</xsl:stylesheet>
