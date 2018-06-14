<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java">
	<xsl:template match="/">
		<xsl:element name="Event">
			<xsl:element name="EventId">
				<xsl:value-of select="name(/*)" />
			</xsl:element>
			<xsl:element name="EventHeader">
				<xsl:element name="tenantId">
					<xsl:text>PAndG</xsl:text>
				</xsl:element>
				<xsl:element name="siteId">
					<xsl:value-of select="//WH_ID" />
				</xsl:element>
				<xsl:element name="EventName">
					<xsl:value-of select="name(/*)" />
				</xsl:element>
			</xsl:element>
			<xsl:element name="EventParam">
				<xsl:copy-of select="/" />
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>