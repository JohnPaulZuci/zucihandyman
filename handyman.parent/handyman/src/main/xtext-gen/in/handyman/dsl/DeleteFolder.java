/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Delete Folder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.DeleteFolder#getFoldersource <em>Foldersource</em>}</li>
 *   <li>{@link in.handyman.dsl.DeleteFolder#getZipsource <em>Zipsource</em>}</li>
 *   <li>{@link in.handyman.dsl.DeleteFolder#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see in.handyman.dsl.DslPackage#getDeleteFolder()
 * @model
 * @generated
 */
public interface DeleteFolder extends Action
{
  /**
   * Returns the value of the '<em><b>Foldersource</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Foldersource</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Foldersource</em>' attribute.
   * @see #setFoldersource(String)
   * @see in.handyman.dsl.DslPackage#getDeleteFolder_Foldersource()
   * @model
   * @generated
   */
  String getFoldersource();

  /**
   * Sets the value of the '{@link in.handyman.dsl.DeleteFolder#getFoldersource <em>Foldersource</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Foldersource</em>' attribute.
   * @see #getFoldersource()
   * @generated
   */
  void setFoldersource(String value);

  /**
   * Returns the value of the '<em><b>Zipsource</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Zipsource</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Zipsource</em>' attribute.
   * @see #setZipsource(String)
   * @see in.handyman.dsl.DslPackage#getDeleteFolder_Zipsource()
   * @model
   * @generated
   */
  String getZipsource();

  /**
   * Sets the value of the '{@link in.handyman.dsl.DeleteFolder#getZipsource <em>Zipsource</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Zipsource</em>' attribute.
   * @see #getZipsource()
   * @generated
   */
  void setZipsource(String value);

  /**
   * Returns the value of the '<em><b>Value</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Value</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Value</em>' attribute.
   * @see #setValue(String)
   * @see in.handyman.dsl.DslPackage#getDeleteFolder_Value()
   * @model
   * @generated
   */
  String getValue();

  /**
   * Sets the value of the '{@link in.handyman.dsl.DeleteFolder#getValue <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Value</em>' attribute.
   * @see #getValue()
   * @generated
   */
  void setValue(String value);

} // DeleteFolder
