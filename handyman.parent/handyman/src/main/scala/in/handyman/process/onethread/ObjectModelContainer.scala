package in.handyman.process.onethread

import scala.collection.mutable.HashSet
import scala.collection.mutable.LinkedHashSet

case class ColumnInARow(columnType: Int, columnTypeName: String, columnName: String, columnLabel: String, scale: Integer, 
    value: Object, columnFunction:String, isLastColumn:Boolean)
case class Row(rowId:Int, columnSet:LinkedHashSet[ColumnInARow])

object ObjectModelContainer {
  
}