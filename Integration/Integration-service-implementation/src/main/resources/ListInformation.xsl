<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java">

	<xsl:template match="/">

		<LIST_INFORMATION>
			<CTRL_SEG>
				<TRANID>
					<xsl:value-of select="//EVENTSEQ" />
				</TRANID>
				<TRANDT>
					<xsl:value-of select="java:util.Date.new()" />
				</TRANDT>
				<WCS_ID>
					<xsl:value-of select="//WCSID" />
				</WCS_ID>
				<WH_ID>
					<xsl:value-of select="//WHID" />
				</WH_ID>

				<xsl:for-each select="root/primeResponse/mocaResults">
					<LIST_INFO_SEG>
						<LISTID>
							<xsl:value-of select="listid" />
						</LISTID>
						<PALLETTYPE>
							<xsl:value-of select="palletType" />
						</PALLETTYPE>
						<LIST_DETAIL_SEG>
							<EARLYSHIPDATE>
								<xsl:value-of select="earlyshipdate" />
							</EARLYSHIPDATE>
							<LYQTY>
								<xsl:value-of select="lyqty" />
							</LYQTY>
							<ITEM>
								<xsl:value-of select="item" />
							</ITEM>
							<QTY>
								<xsl:value-of select="qty" />
							</QTY>
							<UOM>
								<xsl:value-of select="uom" />
							</UOM>
							<PICKINSTRUCTION>
								<xsl:value-of select="pickInstruction" />
							</PICKINSTRUCTION>
							<ORDNUM>
								<xsl:value-of select="ordnum" />
							</ORDNUM>
							<CSTNUM>
								<xsl:value-of select="cstnum" />
							</CSTNUM>
							<LOTNUM />
							<CRUSHINDEX>
								<xsl:value-of select="crushIndex" />
							</CRUSHINDEX>
							<HEIGHT>
								<xsl:value-of select="height" />
							</HEIGHT>
							<WEIGHT>
								<xsl:value-of select="weight" />
							</WEIGHT>
							<LENGTH>
								<xsl:value-of select="length" />
							</LENGTH>
							<WIDTH>
								<xsl:value-of select="width" />
							</WIDTH>
							<APALSEQ>
								<xsl:value-of select="apalseq" />
							</APALSEQ>
							<PICKINGALGO>
								<xsl:value-of select="pickingAlgo" />
							</PICKINGALGO>
						</LIST_DETAIL_SEG>
					</LIST_INFO_SEG>

				</xsl:for-each>
			</CTRL_SEG>
		</LIST_INFORMATION>

	</xsl:template>

</xsl:stylesheet>