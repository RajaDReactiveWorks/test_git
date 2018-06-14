<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:element name="Event">
			<xsl:element name="EventId">
				<xsl:value-of select="//ELASTICEVENT//HEADER//EVENTID" />
			</xsl:element>
			<xsl:element name="EventHeader">
				<xsl:element name="tenantId">
					<xsl:text>PAndG</xsl:text>
				</xsl:element>
				<xsl:element name="siteId">
					<xsl:value-of select="//ELASTICEVENT//HEADER//WHID" />
				</xsl:element>
				<xsl:element name="EventName">
					<xsl:value-of select="//ELASTICEVENT//HEADER//EVENTID" />
				</xsl:element>
			</xsl:element>
			<xsl:element name="EventParam">
				<xsl:copy-of select="//ELASTICEVENT" />
				<xsl:element name="Source">
					<xsl:value-of select="//ELASTICEVENT//HEADER//WCSID" />
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>