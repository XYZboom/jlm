package team.jlm.coderefactor.code

import com.intellij.packageDependencies.ForwardDependenciesBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.xyzboom.algorithm.graph.GEdge
import com.xyzboom.algorithm.graph.GNode
import com.xyzboom.algorithm.graph.Graph
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Factory.graph
import guru.nidi.graphviz.model.Factory.node
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import guru.nidi.graphviz.model.Graph as VizGraph

/**
 * 继承与依赖图 ----- TODO 待完善
 */
open class IG(private var classes: List<PsiClass>) : Graph<String>() {
    private val classWhoseParentsAdded = HashSet<String>()

    init {
        for (clazz in classes) {
            addClassAndParents(clazz)
        }

        //释放Set的内存
        classWhoseParentsAdded.clear()
//        for (vizNode in vizNodes.values) {
//            vizGraph = vizGraph.with(vizNode as LinkSource)
//        }
    }

    private fun addEdge(from: String, to: String) {
//        vizNodes[from.data]?.let {
//            vizGraph = vizGraph.with(it.link(vizNodes[to.data]))
//        }
//        vizGraph = vizGraph.with(node(from).link(node(to)))
        super.addEdge(from, to, 1)
    }

    private fun addDependencyEdge(from: String, to: String) {
//        vizGraph = vizGraph.with(
//            node(from).link(
//                Factory.to(node(to)).with(Style.DASHED)
//            )
//        )
        super.addEdge(from, to, 2)
    }

    private fun addClassAndParents(clazz: PsiClass) {
        //如果这个类的所有子类全部正确添加，则不执行这个函数的剩余部分
        if (classWhoseParentsAdded.contains(clazz.qualifiedName)) {
            return
        }
        val clazzQualifiedName = clazz.qualifiedName
        //不存在当前扫描到的类则添加这个类
        clazzQualifiedName?.let {
            classWhoseParentsAdded.add(it)
            super.addNode(it)
        }
//        clazzQualifiedName?.let { super.addNode(it) }
        if (!classes.contains(clazz) || clazzQualifiedName == null) {//排除不在项目里的类的父类
            return
        }
        val parents = clazz.supers
//         clazz.extendsList?.referenceElements
        //如果一个类的父类在psi中检查不到，说明它是java.lang.Object,因此这里排除了只继承了Object的类
        if (parents.size != 1 || parents[0].supers.isNotEmpty()) {
            for (parent in parents) {
                parent as PsiClass
                if (parent.supers.isEmpty()) {//父类是Object但是子类还实现了其他接口，因此此处忽略Object
                    continue
                }
                addClassAndParents(parent)

                clazz.name?.let {
                    parent.name?.let { it1 ->
                        addEdge(it, it1)
                    }
                }
            }
        }
        ForwardDependenciesBuilder.analyzeFileDependencies((clazz.containingFile as PsiJavaFile))
        { _: PsiElement, psiElement1: PsiElement ->
            if (psiElement1 is PsiClass) {
                psiElement1.name?.let { it1 -> clazz.name?.let { addDependencyEdge(it, it1) } }
            }
        }
//        for (dependency in dependencies) {
////            dependency.
//            if (dependency is PsiClass)
//                dependency.name?.let { it1 -> clazz.name?.let { addDependencyEdge(it, it1) } }
//        }
    }

    fun toGraphvizGraph(): VizGraph {
        var viz = graph().directed()
        for (pair in adjList) {
            for (edgeOut in pair.value.edgeOut) {
                viz = if (edgeOut.length == 1) {
                    viz.with(node(pair.key.data).link(node(edgeOut.nodeTo.data)))
                } else {
                    viz.with(node(pair.key.data).link(
                        Factory.to(node(edgeOut.nodeTo.data)).with(Style.DASHED)
                    ))
                }
            }
        }
        return viz
    }
}
