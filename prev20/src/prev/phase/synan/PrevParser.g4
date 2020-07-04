parser grammar PrevParser;

@header {

        package prev.phase.synan;
        
        import java.util.*;
        
        import prev.common.report.*;
        import prev.phase.lexan.*;
        import prev.phase.lexan.LexAn.PrevToken;
        import prev.data.ast.tree.*;
        import prev.data.ast.tree.expr.*;
        import prev.data.ast.tree.stmt.*;
        import prev.data.ast.tree.decl.*;
        import prev.data.ast.tree.type.*;

}

options{
    tokenVocab=PrevLexer;
}


source returns [AstTrees<AstDecl> ast]    
            : prg EOF { $ast = $prg.ast; } ;

prg	returns [AstTrees<AstDecl> ast]
            : decls { $ast = $decls.ast; };                 
                        
                        
decls returns [AstTrees<AstDecl> ast]
            locals [Vector<AstDecl> allDecls = new Vector<AstDecl>();]
            : (decl {
                $allDecls.add($decl.ast);
                $ast = new AstTrees<AstDecl>($allDecls);
            })+;
	
decl returns [AstDecl ast]
            : d_typ=TYP name=ID EQUALS type {  
                                    Location typeLoc = new Location(((PrevToken) $d_typ).location(), $type.ast.location());
                                    $ast = new AstTypeDecl(typeLoc, $name.getText(), $type.ast);
                                    }
            | d_var=VAR name=ID COLON type {  
                                    Location varLoc = new Location(((PrevToken) $d_var).location(), $type.ast.location());
                                    $ast = new AstVarDecl(varLoc, $name.getText(), $type.ast);
                                    }
            | d_fun=FUN name=ID LBRACE farg RBRACE COLON type EQUALS expr {   
                                    Location funLoc = new Location(((PrevToken) $d_fun).location(), $expr.ast.location());
                                    $ast = new AstFunDecl(funLoc, $name.getText(), $farg.ast, $type.ast, $expr.ast);  
                                    };
      
      
farg returns [AstTrees<AstParDecl> ast]
            locals [Vector<AstParDecl> params = new Vector<AstParDecl>();]
            : name=ID COLON type {
                Location fstParaLoc = new Location(((PrevToken) $name).location(), $type.ast.location());
                $params.add(new AstParDecl(fstParaLoc, $name.getText(), $type.ast));
            } (com=COMMA name=ID COLON type {
                Location currParaLoc = new Location(((PrevToken) $com).location(), $type.ast.location());
                $params.add(new AstParDecl(currParaLoc, $name.getText(), $type.ast));
            })* {
                $ast = new AstTrees<AstParDecl>($params);
            }
            | { $ast = new AstTrees<AstParDecl>($params); }
            ;

type returns [AstType ast]
            : type_rest { $ast = $type_rest.ast; } 
            | type_array { $ast = $type_array.ast; };
            
type_rest returns [AstType ast]
            : key=TYPE_KEY {        Location keyLoc = ((PrevToken) $key).location();
                                    AstAtomType.Type keyType = AstAtomType.Type.valueOf($key.getText().toUpperCase());
                                    $ast = new AstAtomType(keyLoc, keyType);  }
            | id=ID {               Location idLoc = ((PrevToken) $id).location();
                                    $ast = new AstNameType(idLoc, $id.getText());  }
            | str=STREHA type {
                                    Location strLoc = new Location(((PrevToken) $str).location(), $type.ast.location());
                                    $ast = new AstPtrType(strLoc, $type.ast);  }
            | lb=LBRACK name=ID COLON type tyrec rb=RBRACK {   
                                    Vector<AstCompDecl> recs = new Vector<AstCompDecl>();
                                    Location recLoc = new Location(((PrevToken) $name).location(), $type.ast.location());
                                    recs.add(new AstCompDecl(recLoc, $name.getText(), $type.ast));
                                    
                                    if ($tyrec.ast != null) {
                                        recs.addAll($tyrec.ast);
                                    }
                                    
                                    Location wholeRecLoc = new Location(((PrevToken) $lb).location(), ((PrevToken) $rb).location());
                                    $ast = new AstRecType(wholeRecLoc, new AstTrees<AstCompDecl>(recs));
                                    }
            | LBRACE type RBRACE { $ast = $type.ast; } ;
  
type_array returns [AstArrType ast] :
            LSQBRA expr end=RSQBRA type_rest {
                    Location arrLoc = new Location($type_rest.ast.location(), ((PrevToken) $end).location());
                    $ast = new AstArrType(arrLoc, $type_rest.ast, $expr.ast); }
            | LSQBRA expr end=RSQBRA t_arr=type_array {
                    Location arrLoc = new Location($t_arr.ast.location(), ((PrevToken) $end).location());
                    $ast = new AstArrType(arrLoc, $t_arr.ast, $expr.ast); };
  
  
tyrec returns [Vector<AstCompDecl> ast] 
            : com=COMMA name=ID COLON type tyrec {
                                    Vector<AstCompDecl> recs = new Vector<AstCompDecl>();
                                    Location recLoc = new Location(((PrevToken) $name).location(), $type.ast.location());
                                    recs.add(new AstCompDecl(recLoc, $name.getText(), $type.ast));
                                    
                                    if ($tyrec.ast != null) {
                                        recs.addAll($tyrec.ast);
                                    }
                                    
                                    $ast = recs;
                                    }
            | { $ast = null; };

            
expr_where returns [AstExpr ast]
            : first=expr_where WHERE LBRACK decls rb=RBRACK {
                Location whLoc = new Location($first.ast.location(), ((PrevToken) $rb).location());
                $ast = new AstWhereExpr(whLoc, $first.ast, $decls.ast);
            }
            | expr_disj { $ast = $expr_disj.ast; };
            
            
expr_disj returns [AstExpr ast]
            : first=expr_disj DISSYM expr_conj {
                Location disjLoc = new Location($first.ast.location(), $expr_conj.ast.location());
                $ast = new AstBinExpr(disjLoc, AstBinExpr.Oper.OR, $first.ast, $expr_conj.ast);
            }
            | expr_conj { $ast = $expr_conj.ast; };

expr_conj returns [AstExpr ast]
            : first=expr_conj CONSYM expr_rel {
                Location conjLoc = new Location($first.ast.location(), $expr_rel.ast.location());
                $ast = new AstBinExpr(conjLoc, AstBinExpr.Oper.AND, $first.ast, $expr_rel.ast);
            }
            | expr_rel { $ast = $expr_rel.ast; };

expr_rel returns [AstExpr ast]
            : first=expr_add operator=RELSYM second=expr_add {
            Location relLoc = new Location($first.ast.location(), $second.ast.location());
            AstBinExpr.Oper oper = AstBinExpr.Oper.MUL;
            switch ($operator.getText()) {
                case "==": oper = AstBinExpr.Oper.EQU; break;
                case "!=": oper = AstBinExpr.Oper.NEQ; break;
                case "<": oper = AstBinExpr.Oper.LTH; break;
                case ">": oper = AstBinExpr.Oper.GTH; break;
                case "<=": oper = AstBinExpr.Oper.LEQ; break;
                case ">=": oper = AstBinExpr.Oper.GEQ; break;
            }

            $ast = new AstBinExpr(relLoc, oper, $first.ast, $second.ast);
            }
            | expr_add { $ast = $expr_add.ast; };

expr_add returns [AstExpr ast]
            : first=expr_add operator=PLUSMIN expr_mult {
                Location addLoc = new Location($first.ast.location(), $expr_mult.ast.location());           
                AstBinExpr.Oper oper = null;
                if($operator.getText().equals("+")) {
                    oper = AstBinExpr.Oper.ADD;
                }
                else {
                    oper = AstBinExpr.Oper.SUB;
                }
                
                $ast = new AstBinExpr(addLoc, oper, $first.ast, $expr_mult.ast);
            }
            | expr_mult { $ast = $expr_mult.ast; };

expr_mult returns [AstExpr ast]
            : first=expr_mult operator=MULSYM expr_pfx {
                Location multLoc = new Location($first.ast.location(), $expr_pfx.ast.location());
                
                AstBinExpr.Oper oper = null;
                switch ($operator.getText()) {
                    case "*": oper = AstBinExpr.Oper.MUL; break;
                    case "/": oper = AstBinExpr.Oper.DIV; break;
                    case "%": oper = AstBinExpr.Oper.MOD; break;
                }
                
                $ast = new AstBinExpr(multLoc, oper, $first.ast, $expr_pfx.ast);
            }
            | expr_pfx { $ast = $expr_pfx.ast; };

expr_pfx returns [AstExpr ast]
            : operator=(EXCL | PLUSMIN | STREHA | NEW | DEL) expr_pfx {
                Location pfxLoc = new Location(((PrevToken) $operator).location(), $expr_pfx.ast.location());
                
                AstPfxExpr.Oper oper = null;
                switch ($operator.getText()) {
                    case "!": oper = AstPfxExpr.Oper.NOT; break;
                    case "+": oper = AstPfxExpr.Oper.ADD; break;
                    case "-": oper = AstPfxExpr.Oper.SUB; break;
                    case "^": oper = AstPfxExpr.Oper.PTR; break;
                    case "new": oper = AstPfxExpr.Oper.NEW; break;
                    case "del": oper = AstPfxExpr.Oper.DEL; break;
                }

                $ast = new AstPfxExpr(pfxLoc, oper, $expr_pfx.ast);
            }
            | expr_sfx { $ast = $expr_sfx.ast; };

expr_sfx returns [AstExpr ast]
            : first=expr_sfx LSQBRA mid=expr_where end=RSQBRA   {
                Location location = new Location($first.ast.location(), ((PrevToken) $end).location());
                $ast = new AstArrExpr(location, $first.ast, $mid.ast);
            }
            | first=expr_sfx  LSQBRA mid1=expr_rest end=RSQBRA {
                Location location = new Location($first.ast.location(), ((PrevToken) $end).location());
                $ast = new AstArrExpr(location, $first.ast, $mid1.ast);
            }
            | first=expr_sfx LSQBRA mid2=expr_sfx end=RSQBRA {
                Location location = new Location($first.ast.location(), ((PrevToken) $end).location());
                $ast = new AstArrExpr(location, $first.ast, $mid2.ast);
            }
            | first=expr_sfx end=STREHA {
                Location location = new Location($first.ast.location(), ((PrevToken) $end).location());
                $ast = new AstSfxExpr(location, AstSfxExpr.Oper.PTR, $first.ast);
            }
            | first=expr_sfx DOT name=ID {
                Location location = new Location($first.ast.location(), ((PrevToken) $name).location());
                AstNameExpr nameExpr = new AstNameExpr(((PrevToken) $name).location(), $name.getText());
                $ast = new AstRecExpr(location, $first.ast, nameExpr);
            }
            | expr_rest { $ast = $expr_rest.ast; };


expr_id returns [AstNameExpr ast]
            : id=ID { $ast = new AstNameExpr(((PrevToken) $id).location(), $id.getText()); };

            
expr_const returns [AstExpr ast] 
            : e_const=VOIDCONST { 
                $ast = new AstAtomExpr(((PrevToken) $e_const).location(),               AstAtomExpr.Type.VOID, $e_const.getText());   }
            | e_const=INTCONST { 
                $ast = new AstAtomExpr(((PrevToken) $e_const).location(),               AstAtomExpr.Type.INTEGER, $e_const.getText());   }
            | e_const=BOOLCONST { 
                $ast = new AstAtomExpr(((PrevToken) $e_const).location(),               AstAtomExpr.Type.BOOLEAN, $e_const.getText());   }
            | e_const=CHARCONST { 
                String text = $e_const.getText();
                text = text.substring(1, text.length() - 1);
                $ast = new AstAtomExpr(((PrevToken) $e_const).location(),               AstAtomExpr.Type.CHAR, $e_const.getText());   }
            | e_const=STRINGCONST { 
                String text = $e_const.getText();
                text = text.substring(1, text.length() - 1);
                $ast = new AstAtomExpr(((PrevToken) $e_const).location(),               AstAtomExpr.Type.STRING, $e_const.getText());   }
            | e_const=POINTERCONST { 
                $ast = new AstAtomExpr(((PrevToken) $e_const).location(),             AstAtomExpr.Type.POINTER, $e_const.getText());   };
                                    
expr_call returns [AstCallExpr ast]
            : name=ID LBRACE expr_fun_arg end=RBRACE {
                Location location = new Location(((PrevToken) $name).location(), ((PrevToken) $end).location());
                $ast = new AstCallExpr(location, $name.getText(), $expr_fun_arg.ast);
            };

expr_fun_arg returns [AstTrees<AstExpr> ast]
            locals [Vector<AstExpr> exprs = new Vector<AstExpr>()]
            : expr {
                $exprs.add($expr.ast);
                $ast = new AstTrees<AstExpr>($exprs);
            } (COMMA expr {
                $exprs.add($expr.ast);
                $ast = new AstTrees<AstExpr>($exprs);
            })*
            | { $ast = new AstTrees<AstExpr>($exprs); };

expr_comp returns [AstStmtExpr ast]
            locals [Vector<AstStmt> stmts = new Vector<AstStmt>();]
            : first=LBRACK stmt {
                $stmts.add($stmt.ast);
            } SEMICOL (stmt SEMICOL {
                $stmts.add($stmt.ast);
            })* end=RBRACK {
                Location location = new Location(((PrevToken) $first).location(), ((PrevToken) $end).location());
                AstTrees<AstStmt> parsStmts = new AstTrees<AstStmt>($stmts);
                $ast = new AstStmtExpr(location, parsStmts);
            };

expr_cast returns [AstCastExpr ast]
            : first=LBRACE expr COLON type end=RBRACE {
                Location location = new Location(((PrevToken) $first).location(), ((PrevToken) $end).location());
                $ast = new AstCastExpr(location, $expr.ast, $type.ast);
            };

expr_encl returns [AstExpr ast]
            : beg=LBRACE expr end=RBRACE { $ast = $expr.ast; };

expr_rest returns [AstExpr ast]
            : expr_const { $ast = $expr_const.ast; }
            | expr_id { $ast = $expr_id.ast; }
            | expr_call { $ast = $expr_call.ast; }
            | expr_comp { $ast = $expr_comp.ast; }
            | expr_cast { $ast = $expr_cast.ast; }
            | expr_encl { $ast = $expr_encl.ast; };
            
expr returns [AstExpr ast]
            : expr_where {$ast = $expr_where.ast; };
            
            
stmt returns [AstStmt ast]
            : expr { $ast = new AstExprStmt($expr.ast.location(), $expr.ast); }
            | first=expr EQUALS sec=expr{ 
                                        Location eqLoc = new Location($first.ast.location(), $sec.ast.location());
                                        $ast = new AstAssignStmt(eqLoc, $first.ast, $sec.ast);}
            | s_if=IF expr THEN thenSt=stmt ELSE elseSt=stmt {          
                                    Location ifLoc = new Location(((PrevToken) $s_if).location(), $elseSt.ast.location());
                                    $ast = new AstIfStmt(ifLoc, $expr.ast, $thenSt.ast, $elseSt.ast);  }
            | wh=WHILE expr DO stmt {  
                                    Location whLoc = new Location(((PrevToken) $wh).location(), $stmt.ast.location());
                                    $ast = new AstWhileStmt(whLoc, $expr.ast, $stmt.ast);  };
