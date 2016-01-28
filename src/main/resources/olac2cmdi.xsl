<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.clarin.eu/cmd/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:defns="http://www.openarchives.org/OAI/2.0/" xmlns:olac="http://www.language-archives.org/OLAC/1.0/" xmlns:olac11="http://www.language-archives.org/OLAC/1.1/" xsi:schemaLocation="    http://purl.org/dc/elements/1.1/    http://www.language-archives.org/OLAC/1.0/dc.xsd    http://purl.org/dc/terms/    http://www.language-archives.org/OLAC/1.0/dcterms.xsd    http://www.language-archives.org/OLAC/1.0/    http://www.language-archives.org/OLAC/1.0/olac.xsd    http://www.language-archives.org/OLAC/1.0/ http://www.language-archives.org/OLAC/1.0/third-party/software.xsd ">

    <!-- run on Ubuntu with: saxonb-xslt -ext:on -it main ~/svn/clarin/metadata/trunk/toolkit/xslt/olac2cmdi.xsl  -->

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:key name="iso-lookup" match="lang" use="sil"/>

    <!--
	This parameter can be used to specify path to the iso xml
	file.
      -->
    <xsl:param name="iso_xml_path" select="'resources/'"/>
    <xsl:variable name="lang-top" select="document(concat($iso_xml_path, 'sil_to_iso6393.xml'))/languages"/>

    <!--
	Name of the provider the record was harvested from (or empty
	if name is not known).
      -->
    <xsl:param name="provider_name"/>

    <!--
	Identifier of the record.
      -->
    <xsl:param name="record_identifier"/>

    <xsl:template match="text()"/>

    <xsl:template match="/">
            <xsl:if test="empty(//defns:record[defns:header/defns:identifier = $record_identifier])">
                <xsl:message>WRN: no record with identifier[<xsl:value-of select="$record_identifier"/>] found!</xsl:message>
            </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="//defns:record[defns:header/defns:identifier = $record_identifier]">
        <!--
        <xsl:message>DBG: record[<xsl:value-of select="count(preceding::defns:record) + 1"/>/<xsl:value-of select="count(../defns:record)"/>] with identifier[<xsl:value-of select="$record_identifier"/>] found!</xsl:message>
        -->
        <CMD CMDVersion="1.1" xsi:schemaLocation="http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd">
            <Header>
                <MdCreator>olac2cmdi.xsl</MdCreator>
                <MdCreationDate>
                    <xsl:variable name="date">
                        <xsl:value-of select="./defns:header[1]/defns:datestamp[1]"/>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="contains($date, 'T')">
                            <xsl:value-of select="substring-before($date, 'T')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$date"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </MdCreationDate>
                <MdSelfLink>
                    <xsl:value-of select="./defns:header[1]/defns:identifier[1]"/>
                </MdSelfLink>
                <MdProfile>clarin.eu:cr1:p_1288172614026</MdProfile>
                    <xsl:choose>
                        <xsl:when test="normalize-space(./defns:header[1]/defns:setSpec[1]/@setName)!=''">
                            <xsl:choose>
                                <xsl:when test="normalize-space($provider_name)!=''">
                                    <MdCollectionDisplayName>
                                        <xsl:value-of select="$provider_name"/>
                                        <xsl:text>: </xsl:text>
                                        <xsl:value-of select="./defns:header[1]/defns:setSpec[1]/@setName"/>
                                    </MdCollectionDisplayName>
                                </xsl:when>
                                <xsl:otherwise>
                                    <MdCollectionDisplayName>
                                        <xsl:value-of select="./defns:header[1]/defns:setSpec[1]/@setName"/>
                                    </MdCollectionDisplayName>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="normalize-space($provider_name)!=''">
                            <MdCollectionDisplayName>
                                <xsl:value-of select="$provider_name"/>
                            </MdCollectionDisplayName>
                        </xsl:when>
                    </xsl:choose>
            </Header>
            <Resources>
                <ResourceProxyList>
                    <xsl:apply-templates select="./defns:metadata//dc:identifier" mode="preprocess"/>
                </ResourceProxyList>
                <JournalFileProxyList/>
                <ResourceRelationList/>
            </Resources>
            <Components>
                <xsl:variable name="record" select="./defns:metadata"/>
                <OLAC-DcmiTerms>

                    <xsl:apply-templates select="$record//dcterms:abstract" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:accessRights" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:accrualMethod" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:accrualPeriodicity" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:accrualPolicy" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:alternative" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:audience" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:available" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:bibliographicCitation" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:conformsTo" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:contributor" mode="cmd"/>
                    <xsl:apply-templates select="$record//dc:coverage" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:created" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:creator" mode="cmd"/>
                    <xsl:apply-templates select="$record//dc:date" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:dateAccepted" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:dateCopyrighted" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:dateSubmitted" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:description" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:educationLevel" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:extent" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:format" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:hasFormat" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:hasPart" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:hasVersion" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:identifier" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:instructionalMethod" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:isFormatOf" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:isPartOf" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:isReferencedBy" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:isReplacedBy" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:isRequiredBy" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:issued" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:isVersionOf" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:language" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:license" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:mediator" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:medium" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:modified" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:provenance" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:publisher" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:references" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:relation" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:replaces" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:requires" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:rights" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:rightsHolder" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:source" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:spatial" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:subject" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:tableOfContents" mode="cmd"/>
                    <xsl:apply-templates select="$record//dcterms:temporal" mode="cmd"/>

                    <xsl:apply-templates select="$record//dc:title" mode="cmd"/>
                    <xsl:apply-templates select="$record//dc:type" mode="cmd"/>

                    <xsl:apply-templates select="$record//dcterms:valid" mode="cmd"/>

                </OLAC-DcmiTerms>
            </Components>
        </CMD>
    </xsl:template>

    <xsl:template match="dc:contributor" mode="cmd">
        <contributor>
            <xsl:if test="@xsi:type = 'olac:role'">
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

    <xsl:template match="dc:description" mode="cmd">
        <description>
            <xsl:apply-templates select="./@xml:lang" mode="#current"/>
            <xsl:apply-templates select="@xsi:type" mode="#current"/>
            <xsl:value-of select="."/>
        </description>
    </xsl:template>

    <xsl:template match="dc:language[@xsi:type = 'olac:language']" priority="3" mode="cmd">
        <language>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-language">
                    <!-- can be enabled when there is a 1-to-1 mapping in sil_to_iso6393.xml           -->
                    <xsl:choose>
                        <xsl:when test="contains(@*:code, 'x-sil-')">
                            <xsl:apply-templates select="$lang-top" mode="#current">
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

    <xsl:template match="languages" mode="cmd">
        <xsl:param name="curr-label"/>
        <xsl:variable name="silcode">
            <xsl:value-of select="lower-case(replace($curr-label/@*:code, 'x-sil-', ''))"/>
        </xsl:variable>
        <xsl:value-of select="key('iso-lookup', $silcode)/iso"/>
    </xsl:template>


    <xsl:template match="dc:identifier" mode="preprocess">
        <xsl:if test="contains(., 'http://') or contains(., 'https://') or contains(., 'urn:nbn') or contains(., 'hdl:')">
            <ResourceProxy>
                <xsl:attribute name="id">
                    <xsl:value-of select="generate-id()"/>
                </xsl:attribute>
                <ResourceType>Resource</ResourceType>
                <ResourceRef>
                    <xsl:value-of select="."/>
                </ResourceRef>
            </ResourceProxy>
        </xsl:if>
    </xsl:template>


    <xsl:template match="dc:subject[@xsi:type = 'olac:language']" priority="3" mode="cmd">
        <subject>
            <!-- can be enabled when there is a 1-to-1 mapping in sil_to_iso6393.xml           -->
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-language">
                    <xsl:choose>
                        <xsl:when test="contains(@*:code, 'x-sil-')">
                            <xsl:apply-templates select="$lang-top" mode="#current">
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

    <xsl:template match="//dc:subject[@xsi:type = 'olac:linguistic-field']" priority="3" mode="cmd">
        <subject>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-linguistic-field">
                    <xsl:value-of select="@*:code"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>

    <xsl:template match="//dc:subject[@xsi:type = 'olac:discourse-type']" priority="3" mode="cmd">
        <subject>
            <xsl:attribute name="olac-discourse-type">
                <xsl:value-of select="@*:code"/>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>


    <xsl:template match="//dc:subject" priority="1" mode="cmd">
        <subject>
            <xsl:apply-templates select="./@xml:lang" mode="#current"/>
            <xsl:apply-templates select="@xsi:type" mode="#current"/>
            <xsl:value-of select="."/>
        </subject>
    </xsl:template>


    <xsl:template match="//dc:title" mode="cmd">
        <title>
            <xsl:apply-templates select="./@xml:lang" mode="#current"/>
            <xsl:apply-templates select="@xsi:type" mode="#current"/>
            <xsl:value-of select="."/>
        </title>
    </xsl:template>


    <xsl:template match="//dc:type[@xsi:type = 'olac:discourse-type']" priority="2" mode="cmd">
        <type>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-discourse-type">
                    <xsl:value-of select="@*:code"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </type>
    </xsl:template>


    <xsl:template match="//dc:type[@xsi:type = 'olac:linguistic-type']" priority="2" mode="cmd">
        <type>
            <xsl:if test="@*:code">
                <xsl:attribute name="olac-linguistic-type">
                    <xsl:value-of select="@*:code"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </type>
    </xsl:template>


    <xsl:template match="//dc:type" priority="1" mode="cmd">
        <type>
            <xsl:apply-templates select="@xsi:type" mode="#current"/>
            <xsl:value-of select="."/>
        </type>
    </xsl:template>

    <xsl:template match="@xml:lang" mode="cmd">
        <xsl:attribute name="xml:lang">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="@xsi:type" mode="cmd">
        <xsl:variable name="attval">
            <xsl:value-of select="."/>
        </xsl:variable>
        <xsl:if test="contains($attval, 'dcterms:')">
            <xsl:variable name="attclean">
                <xsl:value-of select="replace($attval, 'dcterms:', '')"/>
            </xsl:variable>
            <xsl:attribute name="dcterms-type">
                <xsl:value-of select="$attclean"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!--  general DC  template -->
    <xsl:template match="dc:*" mode="cmd">
        <xsl:variable name="tagname">
            <xsl:value-of select="local-name()"/>
        </xsl:variable>
        <xsl:element name="{$tagname}">
            <xsl:apply-templates select="@xsi:type" mode="#current"/>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

    <!--  general DC terms template -->
    <xsl:template match="dcterms:*" mode="cmd">
        <xsl:variable name="tagname">
            <xsl:value-of select="local-name()"/>
        </xsl:variable>
        <xsl:element name="{$tagname}">
            <xsl:apply-templates select="@xsi:type" mode="#current"/>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
