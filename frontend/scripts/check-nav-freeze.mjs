import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const rootDir = path.resolve(__dirname, '..')

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf-8'))
}

function normalizeMenu(menu) {
  return menu.map((item) => ({
    title: item.title,
    children: (item.children || []).map((child) => child.title)
  }))
}

function normalizeFreezeMenu(menu) {
  return menu.map((item) => ({
    title: item.title,
    children: item.children || []
  }))
}

function assertEqual(actual, expected, label, errors) {
  const a = JSON.stringify(actual)
  const e = JSON.stringify(expected)
  if (a !== e) {
    errors.push(`${label} 不一致`)
    errors.push(`  期望: ${e}`)
    errors.push(`  实际: ${a}`)
  }
}

function extractChildrenPaths(routeFilePath) {
  const content = fs.readFileSync(routeFilePath, 'utf-8')
  const childrenMatch = content.match(/children:\s*\[([\s\S]*?)\]\s*\n\s*\}/)
  if (!childrenMatch) return []
  const childrenBlock = childrenMatch[1]
  const paths = []
  const regex = /path:\s*'([^']+)'/g
  let match = regex.exec(childrenBlock)
  while (match) {
    paths.push(match[1])
    match = regex.exec(childrenBlock)
  }
  return paths
}

async function main() {
  const freezeFile = path.join(rootDir, 'navigation-freeze.json')
  const freeze = readJson(freezeFile)
  const menusModulePath = pathToFileURL(path.join(rootDir, 'src', 'menus', 'index.js')).href
  const menusModule = await import(menusModulePath)

  const roleToMenu = {
    admin: menusModule.adminMenu,
    teacher: menusModule.teacherMenu,
    student: menusModule.studentMenu
  }

  const roleToRouteFile = {
    admin: path.join(rootDir, 'src', 'router', 'routes', 'admin.js'),
    teacher: path.join(rootDir, 'src', 'router', 'routes', 'teacher.js'),
    student: path.join(rootDir, 'src', 'router', 'routes', 'student.js')
  }

  const errors = []

  for (const role of ['admin', 'teacher', 'student']) {
    const freezeRole = freeze.roles[role]
    const currentMenu = roleToMenu[role]
    if (!freezeRole || !currentMenu) {
      errors.push(`${role} 数据缺失`)
      continue
    }

    const actualMenu = normalizeMenu(currentMenu)
    const expectedMenu = normalizeFreezeMenu(freezeRole.menu)
    assertEqual(actualMenu, expectedMenu, `${role} 菜单`, errors)

    const actualRouteChildren = extractChildrenPaths(roleToRouteFile[role])
    assertEqual(actualRouteChildren, freezeRole.routeChildren, `${role} 路由子路径`, errors)
  }

  if (errors.length > 0) {
    console.error('[导航冻结检查失败]')
    for (const err of errors) console.error(err)
    process.exit(1)
  }

  console.log('[导航冻结检查通过] 菜单与路由均符合 navigation-freeze.json')
}

main().catch((err) => {
  console.error('[导航冻结检查异常]', err)
  process.exit(1)
})
