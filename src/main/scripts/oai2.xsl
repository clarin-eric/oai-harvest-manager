<?xml version="1.0" encoding="UTF-8"?>
<!--Stylesheet for OLAC  documents
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:olac="http://www.language-archives.org/OLAC/1.0/olac-extension.xsd" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="1.0">
	<xsl:output method="html" version="4.0" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" doctype-system="http://www.w3.org/TR/REC-html40/loose.dtd" encoding="ISO-8859-1"/>
	<xsl:strip-space elements="*"/>
	<xsl:preserve-space elements="eg"/>
	<xsl:template match="/">
		<HTML>
			<HEAD>
				<TITLE>
					<xsl:value-of select="header/title"/>
				</TITLE>
				<meta name="Title">
					<xsl:attribute name="content"><xsl:value-of select="header/title"/></xsl:attribute>
				</meta>
				<meta name="Creator">
					<xsl:attribute name="content"><xsl:value-of select="header/editors"/></xsl:attribute>
				</meta>
				<meta name="Description">
					<xsl:attribute name="content"><xsl:value-of select="header/abstract"/></xsl:attribute>
				</meta>
				<meta name="Publisher" content="OLAC (Open Language Archives Community)"/>
				<meta name="Date">
					<xsl:attribute name="content"><xsl:value-of select="header/issued"/></xsl:attribute>
				</meta>
				<STYLE>
          td.green       { MARGIN:10px; BACKGROUND: white; COLOR: green; 
                              FONT-FAMILY: sans-serif; FONT-SIZE: 12pt }
          BODY       { MARGIN:10px; BACKGROUND: white; COLOR: navy; 
                              FONT-FAMILY: sans-serif; FONT-SIZE: 12pt }
          H1    {FONT-SIZE: 24pt }
          H2    {FONT-SIZE: 18pt }
          H3    {FONT-SIZE: 16pt }

        </STYLE>
			</HEAD>
			<BODY>
				<TABLE border="1" cellpadding="2" cellspacing="0">
					<col width="10%"/>
					<col width="10%"/>
					<col width="80%"/>
					<xsl:for-each select="//*">
						<xsl:choose>
							<xsl:when test="local-name() = 'OAI-PMH'">
							</xsl:when>
							<xsl:when test="local-name() = 'GetRecord'">
							</xsl:when>
							<xsl:when test="local-name() = 'record'">
							</xsl:when>
							<xsl:when test="local-name() = 'metadata'">
							</xsl:when>
							<xsl:when test="local-name() = 'olac'">
							</xsl:when>
							<xsl:otherwise>
							<tr>
							<td colspan="2">
								<xsl:value-of select="name()"/>
							</td>
							<td>
								<xsl:value-of select="."/>
							</td>
							</tr>
								<xsl:for-each select="@*">
									<!--
									</tr>
									<tr>
									<td>
										stuff
									</td>
									-->
									<tr>
										<td> </td>
									<td class="green">
										<xsl:value-of select="name()"/>
									</td>
									<td class="green">
										<xsl:value-of select="."/>
									</td>
									</tr>
								</xsl:for-each>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</TABLE>
			</BODY>
		</HTML>

	</xsl:template>
</xsl:stylesheet>